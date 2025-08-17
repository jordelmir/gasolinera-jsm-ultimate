provider "aws" {
  region = var.aws_region
}

# --- ECR Repository for Docker Images ---
resource "aws_ecr_repository" "app_repo" {
  name                 = "gasolinera-jsm-ultimate"
  image_tag_mutability = "MUTABLE"

  image_scanning_configuration {
    scan_on_push = true
  }
}

# --- VPC for EKS and RDS ---
module "vpc" {
  source  = "terraform-aws-modules/vpc/aws"
  version = "3.1.0"

  name = "${var.cluster_name}-vpc"
  cidr = "10.0.0.0/16"

  azs             = ["${var.aws_region}a", "${var.aws_region}b"]
  private_subnets = ["10.0.1.0/24", "10.0.2.0/24"]
  public_subnets  = ["10.0.101.0/24", "10.0.102.0/24"]

  enable_nat_gateway = true
  single_nat_gateway = true

  enable_dns_hostnames = true
  enable_dns_support   = true
}

# --- EKS Cluster ---
module "eks" {
  source  = "terraform-aws-modules/eks/aws"
  version = "18.0.0"

  cluster_name    = var.cluster_name
  cluster_version = "1.28"

  vpc_id     = module.vpc.vpc_id
  subnet_ids = module.vpc.private_subnets

  enable_irsa = true

  eks_managed_node_groups = {
    default = {
      min_size     = 2
      max_size     = 3
      desired_size = 2

      instance_types = ["t3.medium"]
      capacity_type  = "ON_DEMAND"
    }
  }
}

# --- RDS PostgreSQL Database ---
resource "aws_db_instance" "postgres_db" {
  allocated_storage    = 20
  engine               = "postgres"
  engine_version       = "16.1"
  instance_class       = "db.t3.micro"
  name                 = var.db_name
  username             = var.db_username
  password             = var.db_password
  port                 = 5432
  multi_az             = false
  skip_final_snapshot  = true
  db_subnet_group_name = aws_db_subnet_group.main.name
  vpc_security_group_ids = [module.eks.cluster_security_group_id]
}

resource "aws_db_subnet_group" "main" {
  name       = "${var.cluster_name}-db-subnet-group"
  subnet_ids = module.vpc.private_subnets
}

# --- ElastiCache Redis ---
resource "aws_elasticache_cluster" "redis_cache" {
  cluster_id           = "${var.cluster_name}-redis"
  engine               = "redis"
  node_type            = "cache.t3.micro"
  num_cache_nodes      = 1
  port                 = 6379
  parameter_group_name = "default.redis6.x"
  engine_version       = "6.x"
  subnet_group_name    = aws_elasticache_subnet_group.main.name
  security_group_ids   = [module.eks.cluster_security_group_id]
}

resource "aws_elasticache_subnet_group" "main" {
  name       = "${var.cluster_name}-elasticache-subnet-group"
  subnet_ids = module.vpc.private_subnets
}

# --- S3 Bucket for Ad Videos ---
resource "aws_s3_bucket" "ad_videos" {
  bucket = "${var.cluster_name}-ad-videos"
  acl    = "private" # Or public-read if videos are served directly

  versioning {
    enabled = true
  }
}