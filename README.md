# 실습을 위한 개발 환경 세팅
* https://github.com/slipp/web-application-server 프로젝트를 자신의 계정으로 Fork한다. Github 우측 상단의 Fork 버튼을 클릭하면 자신의 계정으로 Fork된다.
* Fork한 프로젝트를 eclipse 또는 터미널에서 clone 한다.
* Fork한 프로젝트를 eclipse로 import한 후에 Maven 빌드 도구를 활용해 eclipse 프로젝트로 변환한다.(mvn eclipse:clean eclipse:eclipse)
* 빌드가 성공하면 반드시 refresh(fn + f5)를 실행해야 한다.

# 웹 서버 시작 및 테스트
* webserver.WebServer 는 사용자의 요청을 받아 RequestHandler에 작업을 위임하는 클래스이다.
* 사용자 요청에 대한 모든 처리는 RequestHandler 클래스의 run() 메서드가 담당한다.
* WebServer를 실행한 후 브라우저에서 http://localhost:8080으로 접속해 "Hello World" 메시지가 출력되는지 확인한다.

## docker로 웹서버 환경 구축하기
현재 소스 원격 배포를 위해 시놀로지 NAS를 이용하려 한다. 시놀로지 NAS에서 도커를 지원하므로, NAS의 기본 환경에는 영향을 주지 않고 웹서버 실습을 위해 도커를 사용하여 서버를 개발한다.

Dockerfile을 이용하여 이미지를 생성하였다. 도커파일에서 하는 일은 다음과 같다.
1. 소스 폴더의 pom.xml를 도커 워킹 디렉토리인 /app에 복사한다.
2. maven clean package 커맨드를 실행한다.
3. 현재 디렉토리를 /app에 복사한다.
* Q1: 인터넷으로 검색해서 대충 진행한 것인데 pom.xml만 있으면 maven package를 할 수 있는 것인가? 그럼 3번 과정은 없어도 그만인가?

다음 커맨드를 이용하여 도커파일을 빌드하였다.

    docker build -t web-server-test:0.3 .

*TODO: 이미지 이름이 조금 거시기 한데 나중에 바꿔야겠다.*

그리고 다음 커맨드를 이용하여 컨테이너를 생성하고 실행시킬 수 있다.

    docker run -it -p 1324:80 -v $(pwd):/app web-server-test:0.3 /bin/bash

-p 플래그는 컨테이너 포트를 로컬 호스트에 포워딩하는 옵션이다. NAS에서 80번 포트는 이미 할당이 되어 있기 때문에 사용할 수 없다.
-v 플래그는 호스트의 폴더와 컨테이너의 폴더를 마운트해주는 옵션이다. NAS에서 git으로 소스 업데이트하고 마운트해서 개발 테스트 해볼 수 있을 것 같다.

*TODO: 도커 공식 문서에는 새로 시작하는 사람은 --mount 옵션을 사용하길 권장하고 있다. 나중에 볼륨을 공부하고 바꿔야 할 듯...*

/app/run.sh을 실행시켜 웹서버를 띄울 수 있다. http://<NAS ip>:1324로 접속 가능하다. maven clean package를 할 때마다 의존성 다운로드하는 것이 시간도 많이 걸리고 귀찮았는데 이 부분을 이미지로 구워 놓으니 바뀐 부분만 패키징하면 되서 훨씬 빠른 개발이 가능할 듯 하다.
  
*TODO: 소스가 변경되면 maven package를 실행시켜서 업데이트하면 되지 않을까 생각하고 있다. 테스트해 볼 것.*

# 각 요구사항별 학습 내용 정리
* 구현 단계에서는 각 요구사항을 구현하는데 집중한다. 
* 구현을 완료한 후 구현 과정에서 새롭게 알게된 내용, 궁금한 내용을 기록한다.
* 각 요구사항을 구현하는 것이 중요한 것이 아니라 구현 과정을 통해 학습한 내용을 인식하는 것이 배움에 중요하다. 

### 요구사항 1 - http://localhost:8080/index.html로 접속시 응답
*첫 코딩!!! 첫 성공!!!*
* RequestHandler.java에서 코딩. TODO 태그르 보지 못했다면 어디서부터 시작해야 할 지 한참 헤맸을 것 같다. 책에 스텝별로 힌트가 나와 있어서 따라하기 쉬웠음.
* 맨 처음 InputStream을 -> InputStreamReader -> BufferedReader로 읽어 들여 전체 라인을 출력하는 것부터 진행했다. 콘솔에 System.out.println 함수를 사용할 수 있을지 몰랐는데 사용할 수 있었다.
* 프로젝트를 실행시키고 index.html로 접속하니 GET 명령어 뒤에 상세 주소가 온다는 것을 발견했다.
* InputStream의 라인 중에서 GET으로 시작하는 라인을 찾아 공백으로 tokenize해서 두번째 토큰을 url 변수에 저장하였다.
* url이 "/"이면 이전과 마찬가지로 hello world를 출력하게 했고 아닌 경우 해당 주소의 웹페이지를 byte로 읽어 response 어쩌구 함수들에 넘겨줬다.
** index.html 외에 다른 웹페이지 주소가 넘어오면 에러가 발생할텐데... 예외처리 해 줘야 할 듯.

### 요구사항 2 - get 방식으로 회원가입
* url이 get으로 시작하면 처리되도록 했음
* parameter들을 파싱했어야 했는데 일일히 StringTokenizer로 했으면 엄처 귀찮았을뻔;; 다행히 util.HttpRequestUtils 클래스에 parseQueryString 함수를 이용하여 해결하였다.
* User 클래스의 constructor를 이용하여 user object를 생성
* User 클래스에 toString 함수가 있어서 이를 이용해서 출력하는 것까지 해 보았다.
* TODO: 페이지가 넘어가지 않는데 어떻게 해결해야하지? 뒤에서 해결하는건가?

### 요구사항 3 - post 방식으로 회원가입
* 

### 요구사항 4 - redirect 방식으로 이동
* 

### 요구사항 5 - cookie
* 

### 요구사항 6 - stylesheet 적용
* 

### heroku 서버에 배포 후
* 
