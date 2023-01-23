node('build-slave') {
    try {
        String ANSI_GREEN = "\u001B[32m"
        String ANSI_NORMAL = "\u001B[0m"
        String ANSI_BOLD = "\u001B[1m"
        String ANSI_RED = "\u001B[31m"
        String ANSI_YELLOW = "\u001B[33m"

        ansiColor('xterm') {
          withEnv(["JAVA_HOME=${JAVA11_HOME}"]) {
            stage('Checkout') {
                if (!env.hub_org) {
                    println(ANSI_BOLD + ANSI_RED + "Uh Oh! Please set a Jenkins environment variable named hub_org with value as registery/sunbidrded" + ANSI_NORMAL)
                    error 'Please resolve the errors and rerun..'
                } else
                    println(ANSI_BOLD + ANSI_GREEN + "Found environment variable named hub_org with value as: " + hub_org + ANSI_NORMAL)
                }
            cleanWs()
            if (params.github_release_tag == "") {
		println(ANSI_BOLD + "no release tag / branch, checking out lastest commit" + ANSI_NORMAL)    
                checkout scm
                commit_hash = sh(script: 'git rev-parse --short HEAD', returnStdout: true).trim()
                branch_name = sh(script: 'git name-rev --name-only HEAD | rev | cut -d "/" -f1| rev', returnStdout: true).trim()
                build_tag = branch_name + "_" + commit_hash
                println(ANSI_BOLD + ANSI_YELLOW + "github_release_tag not specified, using the latest commit hash: " + commit_hash + ANSI_NORMAL)
            } else {
		println(ANSI_BOLD + "found release tag / branch, checking out: " + params.github_release_tag + ANSI_NORMAL)    
                def scmVars = checkout scm
		// need to modify
		checkout scm: [$class: 'GitSCM', branches: [[name: "*/$params.github_release_tag"]], userRemoteConfigs: [[url: scmVars.GIT_URL]]]    
                // checkout scm: [$class: 'GitSCM', branches: [[name: "refs/tags/$params.github_release_tag"]], userRemoteConfigs: [[url: scmVars.GIT_URL]]]
                build_tag = params.github_release_tag
                println(ANSI_BOLD + ANSI_YELLOW + "github_release_tag specified, building from tag: " + params.github_release_tag + ANSI_NORMAL)
            }
            echo "build_tag: " + build_tag
	    println(ANSI_BOLD + "checking folder contains with ls" + ANSI_NORMAL)	  
            sh("ls")
            stage('Build') {
		        currentDir = sh(returnStdout: true, script: 'pwd').trim()
                env.NODE_ENV = "build"
                print "Environment will be : ${env.NODE_ENV}"
                sh 'git log -1'
	
		        sh "cd $currentDir"
		        // Build the dependencies for sunbird user-org service
                sh 'mvn clean install -DskipTests'
            }

            stage('Package') {
		        // Create a deployment package
                dir('service') {
                    sh 'mvn play2:dist'
		            sh 'cp target/cert-service-1.2.0-dist.zip ../'
                }
                sh('chmod 777 ./build.sh')
                sh("./build.sh ${build_tag} ${env.NODE_NAME} ${hub_org}")
            }

            stage('ArchiveArtifacts') {
                archiveArtifacts "metadata.json"
                currentBuild.description = "${build_tag}"
            }
          }
        }
    }
    catch (err) {
        currentBuild.result = "FAILURE"
        throw err
    }
}
