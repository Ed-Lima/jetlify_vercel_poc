variable "IAC_branch" {
  description = "The name of the sourcecode branch"
}

variable "bucketName" {
  description = "The bucketName"
}

variable "viewerArn" {
  default = null
  description = "The ARN of the Cloudfront function which has HTTP Authentication defined"
}

variable "certArn" {
  default = ""
  description = "The ARN of the AWS Certificate Manager"
}

variable "altNames" {
  default = []
  description = "Extra CNAMEs (alternate domain names), for the distribution"
}

variable "aws_account_id" {
  default = "881808428467"
}

variable "vpcx_account_id" {
  default = "itx-cdo"
}

variable "region" {
  default = "us-east-1"
}

variable "app_name" {
  default = "jetflify_static"
}

variable "app_version" {
  default = "0.0.0"
}