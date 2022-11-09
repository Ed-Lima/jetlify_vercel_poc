#!/bin/groovy
@Library(["jpm_shared_lib@1.x"]) _
import org.jnj.*
def args = [:]
args.debug = true
args.cleanWorkspace = true
args.manifestSourcesFile = '_scm_jpm/manifest-sources.yaml'
args.environmentMappingFile = '_scm_jpm/environment-mapping.yaml'

new pipelines.stdPipeline().execute(args)