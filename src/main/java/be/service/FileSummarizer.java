package be.service;

import be.domain.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.content.Media;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.MimeType;

@Service
@RequiredArgsConstructor
@Slf4j
public class FileSummarizer {
    private final ChatClient chatClient;
    private final FileIO fileIO;

    private static final String systemPrompt, userInputPrompt;

    static {
        systemPrompt = """
                당신은 문서 요약의 전문가입니다.
                당신의 업무는 주어진 문서의 이름, 내용 등을 상세히 분석하여 **문서를 한 줄로, 매우 간결하게 요약하는 것입니다.**
                
                문서 요약은 **사용자가 정보를 빠르게 파악할 수 있게 제공** 되어야 하며,
                **그럼에도 문서의 주요 목적이나 내용이 생략되어선 안됩니다.**
                
                간단한 예시를 드리겠습니다.
                
                ai 추천 제목 예시
                
                2025년 건강검진 결과
                카카오 계정(ID/PW) 정보
                신한카드 12월 결제 내역 전체 리스트
                스타벅스 커피 기프티콘 (26년 1월 5일 만료)
                2025년 시드니 여행 일정과 숙소 정보(blog)
                서울 원룸 이사 시 체크해야 할 항목 10가지
                보리 중성화 수술 후 주의사항 안내
                아이폰 16 구매 전 스펙 비교 정리
                신규 프로젝트 온보딩일정 안내 문서
                냉장고를 부탁해 간장계란밥 레시피 정리
                요즘 z세대 소비 트렌드 관련 뉴스
                필름 카메라 입문자를 위한 추천 모델
                바이브코딩으로 웹사이트 만들기 강의안
                26년 1월 인바디 체성분 결과
                여권 사본 (유효기간 확인용)
                주민등록등본 (26.01.15 발급)
                토스 계좌 송금 확인증 (완료)
                25년 연말정산 간소화 내역
                투썸 케이크 (~26.05.20 만료)
                인천-나리타 항공권 탑승권
                전세 임대차 계약서 (확정일자)
                0115 마케팅 전략 회의 정리
                원팬 토마토 파스타 레시피
                지하 2층 A-04 구역 주차위치
                """;

        userInputPrompt = """
                다음 제공된 정보는 문서의 정보입니다.
                문서의 내용을 분석해,** 한국어로 한 줄로 요약해 주세요.**
                문장의 길이가 20자를 초과하면 안됩니다.
                """;
    }

    public String summarize(File file) {
        Media media = new Media(getMimeType(file), getFileResource(file));
        log.info(media.toString());
        log.info(media.getMimeType().toString());
        return chatClient.prompt()
                .system(s -> s.text(systemPrompt)
                )
                .user(u -> u.text(userInputPrompt)
                        .media(new Media(getMimeType(file), getFileResource(file)))
                )
                .call()
                .content();
    }

    private MimeType getMimeType(File file) {
        return MimeType.valueOf(file.getFileMediaType().toString());
    }

    private Resource getFileResource(File file) {
        return new ByteArrayResource(fileIO.getFileData(file.getSavedFileName()));
    }

}
