output "eks_cluster_name" {
  description = "Name of the EKS cluster"
  value       = module.eks.cluster_name
}

output "eks_cluster_endpoint" {
  description = "Endpoint of the EKS cluster"
  value       = module.eks.cluster_endpoint
}

output "rds_postgres_endpoint" {
  description = "Endpoint of the RDS PostgreSQL database"
  value       = aws_db_instance.postgres_db.address
}

output "elasticache_redis_endpoint" {
  description = "Endpoint of the ElastiCache Redis cluster"
  value       = aws_elasticache_cluster.redis_cache.cache_nodes[0].address
}

output "s3_ad_videos_bucket_name" {
  description = "Name of the S3 bucket for ad videos"
  value       = aws_s3_bucket.ad_videos.bucket
}

output "ecr_repository_url" {
  description = "URL of the ECR repository"
  value       = aws_ecr_repository.app_repo.repository_url
}
