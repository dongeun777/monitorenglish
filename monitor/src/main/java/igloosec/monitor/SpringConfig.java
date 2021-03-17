package igloosec.monitor;

import igloosec.monitor.repository.MemberRepository;
import igloosec.monitor.repository.MemoryMemberRepository;
import igloosec.monitor.service.MemberService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SpringConfig {
    @Bean
    public MemberService memberService() {
        return new MemberService(memberRepository());
    }
    @Bean
    public MemberRepository memberRepository() {
        return new MemoryMemberRepository();
    }
}