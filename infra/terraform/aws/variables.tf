variable "aws_region" {
  description = "AWS region for the deployment"
  type        = string
  default     = "us-east-1"
}

variable "cluster_name" {
  description = "Name of the EKS cluster and associated resources"
  type        = string
  default     = "gasolinera-jsm-cluster"
}

variable "db_name" {
  description = "Name of the PostgreSQL database"
  type        = string
  default     = "puntogdb"
}

variable "db_username" {
  description = "Username for the PostgreSQL database"
  type        = string
  default     = "puntogadmin"
}

variable "db_password" {
  description = "Password for the PostgreSQL database"
  type        = string
  sensitive   = true
}
