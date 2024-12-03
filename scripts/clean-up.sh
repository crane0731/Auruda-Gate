
# 삭제할 대상 디렉토리 지정
TARGET_DIR="/home/ubuntu/action/auruda-gate"

echo "Cleaning up files in $TARGET_DIR..."
rm -rf $TARGET_DIR/*  # 지정된 디렉토리 내부 파일만 삭제
echo "Clean-up completed in $TARGET_DIR."