#!groovy

pipeline {
	agent {node {label 'master'}}

	environment {
		PATH="/bin:/sbin:/usr/bin:/usr/sbin:/usr/local/bin"
	}

	parameters {
		choice(
			choices: 'dev\nprod',
			description: 'Choose deploy environment',
			name: 'deploy_env'
		)
		string (name: 'branch', defaultValue: 'master', description: 'fill in your ansible repo branch')
	}

	stages {
		stage ("Pull deploy code") {
			steps {
				sh 'git config --global http.sslVerify false'
				dir ("${env.WORKSPACE}"){
					git branch: 'master', credentialsId:'请进行修改',
					url: 'https://gitlab.example.com/root/demo-repo.git'
				}
			}
		}

		stage ("Check env") {
			steps {
				sh """
				set +x
				user=`whoami`
				if [ $user == deploy ]
				then
					echo "[INFO] Current deployment user is $user"
					source /home/deploy/.py3-a2.8-env/bin/activate
					source /home/deploy/.py3-a2.8-env/ansible/hacking/env-setup -q
					echo "[INFO] Current Python version "
					python --version
					echo "[INFO] Current ansible version"
					ansible-playbook --version
					echo "[INFO] Remote system disk space"
					ssh root@test.example.com df -h
					echo "[INFO] Remote system RAM"
					ssh root@test.example.com free -m
				else
					echo "Deployment user is incorrect, please check"
				fi		
				set -x
				"""
			}
		}

		stage ("Ansible deployment") {
			steps {
				input "Do you approve the deployment?"
				dir("${env.WORKSPACE}/wordpress_playbooks"){
					echo "[INFO] Start deployment"
					sh """
					set +x
					source /home/deploy/.py3-a2.8-env/bin/activate
					source /home/deploy/.py3-a2.8-env/ansible/hacking/env-setup -q
					ansible-playbook -i inventory/$deploy_env ./deploy.yml -e project=wordpress -e branch=$branch -e env=$deploy_env
					set -x
					"""
					echo "[INFO] Deployment finished..."
				}
			}
		}
	}
}