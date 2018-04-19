FROM digitalgenius/alpine-python3-pg AS build
MAINTAINER Jeremy Poff

#RUN apt-get update -y \




EXPOSE 5000

WORKDIR /volcard

RUN pip3 install --upgrade pip
RUN pip3 install bcrypt pycryptodome flask pymysql

ADD . /volcard
RUN pip3 install -r requirements.txt


#ENTRYPOINT ["bash"]
#ENTRYPOINT ["python3"]
CMD ["python3", "app/app.py"]
