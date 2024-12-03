#!/bin/bash

# 환경 변수 설정
S3_BUCKET_NAME="craneawsbucket"
SERVER_NAME="auruda-gate"                 # 처리할 서버 디렉토리 이름
LOCAL_PATH="/home/ubuntu/action"
SERVER_PATH="$LOCAL_PATH/$SERVER_NAME"    # 특정 서버 디렉토리
BUILD_PATH="$SERVER_PATH/build/libs"
TEMPLATE_FILE="$SERVER_PATH/application.yml.template"
YML_FILE="$SERVER_PATH/application.yml"

# S3에서 최신 ZIP 파일 이름 가져오기
echo ">>> S3에서 최신 ZIP 파일 이름 검색 중..." >> $LOCAL_PATH/deploy.log
ZIP_FILE_NAME=$(aws s3 ls s3://$S3_BUCKET_NAME/ | sort | tail -n 1 | awk '{print $4}')

if [ -z "$ZIP_FILE_NAME" ]; then
  echo ">>> S3 버킷에서 ZIP 파일을 찾을 수 없습니다." >> $LOCAL_PATH/deploy.log
  exit 1
fi

echo ">>> 찾은 ZIP 파일 이름: $ZIP_FILE_NAME" >> $LOCAL_PATH/deploy.log

# S3에서 zip 파일 다운로드
echo ">>> S3에서 zip 파일 다운로드 중..." >> $LOCAL_PATH/deploy.log
aws s3 cp s3://$S3_BUCKET_NAME/$ZIP_FILE_NAME $LOCAL_PATH/$ZIP_FILE_NAME

# zip 파일 추출
echo ">>> zip 파일 추출 중..." >> $LOCAL_PATH/deploy.log
unzip -o $LOCAL_PATH/$ZIP_FILE_NAME -d $SERVER_PATH/

# clean-up.sh 실행 (압축 해제 후 정리)
if [ -f "$SERVER_PATH/scripts/clean-up.sh" ]; then
  echo ">>> clean-up.sh 실행 중..." >> $LOCAL_PATH/deploy.log
  bash $SERVER_PATH/scripts/clean-up.sh
else
  echo ">>> clean-up.sh 파일이 없습니다. 정리 작업을 생략합니다." >> $LOCAL_PATH/deploy.log
fi

# yml.template 파일이 없으면 생성
if [ ! -f "$TEMPLATE_FILE" ]; then
  echo ">>> yml.template 파일이 없습니다. 새로운 yml.template 파일을 생성 중..." >> $LOCAL_PATH/deploy.log
  cat <<EOL > $TEMPLATE_FILE
server:
  port: 8080
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/db
    username: user
    password: password
EOL
  echo ">>> yml.template 파일 생성 완료!" >> $LOCAL_PATH/deploy.log
else
  echo ">>> yml.template 파일이 이미 존재합니다." >> $LOCAL_PATH/deploy.log
fi

# yml.template 파일을 yml로 복사
echo ">>> yml.template 파일을 yml 파일로 복사 중..." >> $LOCAL_PATH/deploy.log
cp $TEMPLATE_FILE $YML_FILE

# JAR 파일 확인
BUILD_JAR=$(ls $BUILD_PATH/*.jar 2>/dev/null)
if [ -z "$BUILD_JAR" ]; then
  echo ">>> JAR 파일이 존재하지 않습니다. 빌드를 확인하세요." >> $LOCAL_PATH/deploy.log
  exit 1
fi

JAR_NAME=$(basename $BUILD_JAR)
echo ">>> build 파일명: $JAR_NAME" >> $LOCAL_PATH/deploy.log

# 기존 애플리케이션 종료
echo ">>> 현재 실행중인 애플리케이션 pid 확인" >> $LOCAL_PATH/deploy.log
CURRENT_PID=$(pgrep -f $JAR_NAME)
if [ -z "$CURRENT_PID" ]; then
  echo ">>> 현재 구동중인 애플리케이션이 없으므로 종료하지 않습니다." >> $LOCAL_PATH/deploy.log
else
  echo ">>> kill -15 $CURRENT_PID" >> $LOCAL_PATH/deploy.log
  kill -15 $CURRENT_PID
  sleep 5
fi

# 새 JAR 파일 실행
DEPLOY_JAR=$BUILD_JAR
echo ">>> DEPLOY_JAR 배포 중: $DEPLOY_JAR" >> $LOCAL_PATH/deploy.log
nohup java -jar $DEPLOY_JAR --spring.config.location=$YML_FILE >> $SERVER_PATH/deploy.log 2>&1 &

echo ">>> 배포 완료!" >> $LOCAL_PATH/deploy.log
