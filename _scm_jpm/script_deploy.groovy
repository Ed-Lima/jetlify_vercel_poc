#!/bin/groovy
def run(jobManifest) {
  pPrint.info "execute script_build.groovy"
  debug.info()
  def props = [:]
  def jobVars = jobManifest.getJobVars()
  ensure.insideDockerContainer(jobVars.npm.dockerImage) {
    try {      
      dir('project') {
        // install dependencies
        sh 'node -v'
        sh 'npm install --production'
        // build project
        sh './node_modules/.bin/vercel pull --token='
        sh './node_modules/.bin/vercel build --token='
        sh './node_modules/.bin/vercel deploy --token='
      }
    }
    catch(e) {
      pPrint.info "An error occurred while running vercel build: " + e
      throw new Exception("An error occurred while running vercel build: ", e)
    }
  }
}

return this;