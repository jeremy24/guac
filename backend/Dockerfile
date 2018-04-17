FROM digitalgenius/alpine-python3-pg AS build
MAINTAINER Jeremy Poff

#RUN apt-get update -y \

ADD requirements.txt /volcard
WORKDIR /volcard

EXPOSE 5000

RUN pip3 install -r requirements.txt

#ENTRYPOINT ["bash"]
ENTRYPOINT ["python3"]
CMD ["app/app.py"]
