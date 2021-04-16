package igloosec.monitor.service;


import igloosec.monitor.mapper.MemberMapper;
import igloosec.monitor.vo.MemberVo;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;


import java.util.List;


@Service
public class MemberService implements UserDetailsService {
    public final MemberMapper mapper;

    public MemberService(MemberMapper mapper) {
        this.mapper = mapper;
    }


/*    public MemberVo selectMember(MemberVo memberVo) {
        System.out.println(memberVo.getEmail());
        System.out.println(memberVo.getPasswd());

        return mapper.selectMember(username);
    }*/

    public void joinUser(MemberVo memberVo) {
        // 비밀번호 암호화
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        memberVo.setPasswd(passwordEncoder.encode(memberVo.getPasswd()));
        mapper.joinMember(memberVo);
       // return memberRepository.save(memberDto.toEntity()).getId();
    }


    @Override
    public UserDetails  loadUserByUsername(String username) throws UsernameNotFoundException {

        MemberVo user =  mapper.selectMember(username);

        if(user==null) {

            // throw new UsernameNotFoundException(username);
        }
        return user;



    }

}