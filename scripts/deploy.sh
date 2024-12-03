#!/bin/bash

# 환경 변수 설정
S3_BUCKET_NAME="craneawsbucket"
SERVER_NAME="auruda-gate"                 # 처리할 서버 디렉토리 이름
LOCAL_PATH="/home/ubuntu/action"
SERVER_PATH="$LOCAL_PATH/$SERVER_NAME"    # 특정 서버 디렉토리
BUILD_PATH="$SERVER_PATH/build/libs"
TEMPLATE_FILE="$SERVER_PATH/application.yml.template"
YML_FILE="$SERVER_PATH/application.yml"
LOG_FILE="$SERVER_PATH/deploy.log"        # 로그 파일 경로를 auruda-gate 디렉토리로 변경

# 로그 파일 초기화
touch $LOG_FILE
chmod 777 $LOG_FILE

# S3에서 최신 ZIP 파일 이름 가져오기
echo ">>> S3에서 최신 ZIP 파일 이름 검색 중..." >> $LOG_FILE
ZIP_FILE_NAME=$(aws s3 ls s3://$S3_BUCKET_NAME/ | sort | tail -n 1 | awk '{print $4}')

if [ -z "$ZIP_FILE_NAME" ]; then
  echo ">>> S3 버킷에서 ZIP 파일을 찾을 수 없습니다." >> $LOG_FILE
  exit 1
fi

echo ">>> 찾은 ZIP 파일 이름: $ZIP_FILE_NAME" >> $LOG_FILE

# S3에서 zip 파일 다운로드
echo ">>> S3에서 zip 파일 다운로드 중..." >> $LOG_FILE
aws s3 cp s3://$S3_BUCKET_NAME/$ZIP_FILE_NAME $LOCAL_PATH/$ZIP_FILE_NAME

# zip 파일 추출 전 기존 파일 삭제
echo ">>> 기존 파일 삭제 중..." >> $LOG_FILE
if [ -d "$SERVER_PATH" ]; then
  rm -rf "$SERVER_PATH/*"
  echo ">>> 기존 파일 삭제 완료." >> $LOG_FILE
else
  echo ">>> 기존 디렉토리가 없습니다. 새로 생성합니다." >> $LOG_FILE
  mkdir -p "$SERVER_PATH"
fi

# zip 파일 추출
echo ">>> zip 파일 추출 중..." >> $LOG_FILE
unzip -o $LOCAL_PATH/$ZIP_FILE_NAME -d $SERVER_PATH/

# yml.template 파일 생성 확인 및 복사
if [ ! -f "$TEMPLATE_FILE" ]; then
  echo ">>> yml.template 파일이 없습니다. 새로운 yml.template 파일을 생성 중..." >> $LOG_FILE
  cat <<EOL > $TEMPLATE_FILE
server:
  port: 8080
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/db
    username: user
    password: password
EOL
  echo ">>> yml.template 파일 생성 완료!" >> $LOG_FILE
else
  echo ">>> yml.template 파일이 이미 존재합니다." >> $LOG_FILE
fi

# yml.template 파일을 yml로 복사
echo ">>> yml.template 파일을 yml 파일로 복사 중..." >> $LOG_FILE
cp $TEMPLATE_FILE $YML_FILE

# JAR 파일 확인
BUILD_JAR=$(ls $BUILD_PATH/*.jar 2>/dev/null)
if [ -z "$BUILD_JAR" ]; then
  echo ">>> JAR 파일이 존재하지 않습니다. 빌드를 확인하세요." >> $LOG_FILE
  exit 1
fi

JAR_NAME=$(basename $BUILD_JAR)
echo ">>> build 파일명: $JAR_NAME" >> $LOG_FILE

# 기존 애플리케이션 종료
echo ">>> 현재 실행중인 애플리케이션 pid 확인" >> $LOG_FILE
CURRENT_PID=$(pgrep -f $JAR_NAME)
if [ -z "$CURRENT_PID" ]; then
  echo ">>> 현재 구동중인 애플리케이션이 없으므로 종료하지 않습니다." >> $LOG_FILE
else
  echo ">>> kill -15 $CURRENT_PID" >> $LOG_FILE
  kill -15 $CURRENT_PID
  sleep 5
fi

# 새 JAR 파일 실행
DEPLOY_JAR=$BUILD_JAR
echo ">>> DEPLOY_JAR 배포 중: $DEPLOY_JAR" >> $LOG_FILE
nohup java -jar $DEPLOY_JAR --spring.config.location=$YML_FILE >> $LOG_FILE 2>&1 &

echo ">>> 배포 완료!" >> $LOG_FILE
