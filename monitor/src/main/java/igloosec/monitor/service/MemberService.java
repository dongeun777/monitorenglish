package igloosec.monitor.service;


import igloosec.monitor.mapper.MemberMapper;
import igloosec.monitor.vo.MemberVo;
import igloosec.monitor.vo.UsageVo;
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




    public void joinUser(MemberVo memberVo) {
        // 비밀번호 암호화
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        memberVo.setPasswd(passwordEncoder.encode(memberVo.getPasswd()));
        mapper.joinMember(memberVo);

        /*if (memberVo.getAuth().equals("ROLE_USER")){
            mapper.createTmTable(memberVo);
        }*/

       // return memberRepository.save(memberDto.toEntity()).getId();


        //1단계 사용자테이블 rscgrp - tm5SArsg
        //2단계 for is_stats_+       select rscgrp, event_date, event_volume
    }

    public void deleteUser(MemberVo memberVo) {
        mapper.deleteMember(memberVo);
        mapper.deleteEquipList(memberVo);
        mapper.deleteFedTable(memberVo);
    }

    public void modifyUser(MemberVo memberVo) {
        // 비밀번호 암호화
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        memberVo.setPasswd(passwordEncoder.encode(memberVo.getPasswd()));

        mapper.updatePw(memberVo);

    }

    public UsageVo setDeletePath(){
        return mapper.selectDeletePath();
    }



    public void modifyGrpIp(MemberVo memberVo) {

        mapper.modifyGrpIpMember(memberVo);

    }





    @Override
    public UserDetails  loadUserByUsername(String username) throws UsernameNotFoundException {

        MemberVo user =  mapper.selectMember(username);

        if(user==null) {

            // throw new UsernameNotFoundException(username);
        }
        return user;



    }

    public List<MemberVo> selectMemberList() {
        return mapper.selectMemberList();
    }

}