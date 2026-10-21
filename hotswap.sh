#!/bin/sh
# 실행 중인 게임(./gradlew runClient -Photswap)에 바뀐 코드를 바로 반영
# 메서드 안의 코드 변경만 가능. 필드/메서드/클래스 추가는 게임 재시작 필요.
cd "$(dirname "$0")"
export JAVA_HOME="${JAVA_HOME:-$HOME/.jdks/jdk-21.0.12.1+1}"
./gradlew compileClientJava processClientResources -q || exit 1

DIR=build/classes/java/client
{
	sleep 2
	find "$DIR" -name '*.class' | while read -r f; do
		cls=$(echo "${f#$DIR/}" | sed 's/\.class$//; s|/|.|g')
		echo "redefine $cls $f"
	done
	sleep 2
	echo exit
} | "$JAVA_HOME/bin/jdb" -connect com.sun.jdi.SocketAttach:hostname=127.0.0.1,port=5005 2>&1 \
	| grep -iE "error|fail|not|unsupported" && echo "핫스왑 실패: 게임을 재시작하세요." || echo "핫스왑 완료"
