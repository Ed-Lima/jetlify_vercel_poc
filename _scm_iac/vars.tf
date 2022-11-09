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