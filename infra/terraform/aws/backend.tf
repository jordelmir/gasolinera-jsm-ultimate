terraform {
  backend "s3" {
    bucket         = "gasolinera-jsm-terraform-state"
    key            = "terraform.tfstate"
    region         = "us-east-1"
    encrypt        = true
    dynamodb_table = "gasolinera-jsm-terraform-locks"
  }
}
