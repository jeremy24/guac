#!/bin/sh


# launch mongo instance

MY_SQL="mysql"

SQL_PASS="password"
SQL_PORT=3306
SQL_NAME="mariadb"
SQL_VOLUME="/home/jeremy/volcard/database"



MARIADB_DOCKER="jsurf/rpi-mariadb"



MEM='512m'
CPUS=0.000


alias install="sudo apt install -y"
alias dckrun="sudo docker run"
alias dckstop="sudo docker stop"
alias dckrm="sudo docker rm"
alias fw="sudo iptables"


mkdir -p ${SQL_VOLUME}

# kill it first
dckstop ${SQL_NAME}
dckrm ${SQL_NAME}


# launch db
dckrun --memory=${MEM} --cpus=${CPUS}   -v ${SQL_VOLUME}:/var/lib/mysql --restart always  -p ${SQL_PORT}:3306 --name ${SQL_NAME} -e MYSQL_ROOT_PASSWORD=${SQL_PASS} -d ${MARIADB_DOCKER}

# install local mysql client
install ${MY_SQL}


fw -A INPUT -p tcp --dport ${SQL_PORT} --jump ACCEPT
sudo iptables-save

#fw --add-port $SQL_PORT/tcp --permanent
#fw --reload

