#FROM digitalgenius/alpine-python3-pg AS build
FROM ubuntu
MAINTAINER Jeremy Poff

RUN apt-get update;      \
    apt-get upgrade -y;

RUN apt-get install -y python3 python3-pip build-essential;
RUN apt-get install -y libssl-dev libffi-dev python-dev;


EXPOSE 5000

WORKDIR /volcard

RUN pip3 install --upgrade pip
RUN pip3 install bcrypt pycryptodome flask pymysql

ADD . /volcard
RUN pip3 install -r requirements.txt


#ENTRYPOINT ["bash"]
#ENTRYPOINT ["python3"]
CMD ["python3", "app/app.py"]
