module "static-application-hosting" {
  source = "git::https://sourcecode.jnj.com/scm/asx-iacx/terraform-aws-static-application-hosting.git?ref=feature/AGHE-2738"

  # S3 bucket name
  aws_s3_bucket = var.bucketName
  aliases = var.altNames
  viewer_function_arn = var.viewerArn
  certificate_arn = var.certArn
  cache_policy_id = ""
  response_headers_policy_id = ""
  website_error_document = "404/index.html"
}

output "sah_outputs" {
  value = module.static-application-hosting
}

# Terraform State File Location

terraform {
  backend "s3" {
    bucket = "itx-cdo-terraform-us-east-1"
    key    = "jetlify_static/terraform.tfstate"
    region = "us-east-1"
    encrypt = true
  }
}

# Variables

locals {
  env = lower(terraform.workspace)
  app_version = var.app_version
  # Serverless Stack Configs
  agw_name = "${var.app_name}-${local.env}"
  agw_stage_name = "v1"
  agw_role_arn = "arn:aws:iam::${var.aws_account_id}:role/${var.vpcx_account_id}-services-APIGatewayRole"
  lambda_name = "${var.app_name}-${local.env}"
  lambda_memory_size = var.lambda_memory_size
  lambda_runtime = var.lambda_runtime
  lambda_timeout = var.lambda_timeout
  lambda_vpc_subnets = var.lambda_vpc_subnets
  lambda_role_arn = "arn:aws:iam::${var.aws_account_id}:role/${var.vpcx_account_id}-services-VPCxLambdaRole"
  lambda_layer_name = "${local.lambda_name}-layer"
}

# VPC

data "aws_security_groups" "security_groups" {
  filter {
    name   = "group-name"
    values = ["*default*"]
  }
  filter {
    name   = "vpc-id"
    values = [var.vpc_id]
  }
}

# API Gateway

module "api-gateway" {
  source = "./modules/api-gateway"
  # plain variables to override api-gateway/variables.tf
  name = local.agw_name
  description = "Test API"
  stage_name = "v1"
  # referenced variables from lambda/output.tf
  invoke_arn = module.lambda.invoke_arn
}

# Lambda Layer Prep

data "archive_file" "lambda_layer_zip" {
  type = "zip"
  source_dir = "../layer"
  output_path = "./layer.zip"
}

data "archive_file" "lambda_function_zip" {
  type = "zip"
  source_dir = "../src"
  output_path = "./function.zip"
}

# Lambda Function

resource "aws_lambda_layer_version" "lambda_layer" {
  layer_name = local.lambda_layer_name
  filename = data.archive_file.lambda_layer_zip.output_path
  source_code_hash = data.archive_file.lambda_layer_zip.output_base64sha256
  compatible_runtimes = [local.lambda_runtime]
}

module "lambda" {
  source = "./modules/lambda"
  # plain variables to override lambda/variables.tf
  function_name = local.lambda_name
  filename = data.archive_file.lambda_function_zip.output_path
  source_code_hash = data.archive_file.lambda_function_zip.output_base64sha256
  layers = [
    aws_lambda_layer_version.lambda_layer.arn
  ]
  handler = "lambda-wrapper.handler"
  memory_size = local.lambda_memory_size
  runtime = local.lambda_runtime
  timeout = local.lambda_timeout
  role = local.lambda_role_arn
  vpc_config = {
    subnet_ids          = local.lambda_vpc_subnets
    security_group_ids  = data.aws_security_groups.security_groups.ids
  }
  variables = {
    ENVIRONMENT = local.env
    APP_VERSION = local.app_version
  }
  # referenced variables from api-gateway/output.tf
  execution_arn = module.api-gateway.execution_arn
}