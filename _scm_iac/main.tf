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