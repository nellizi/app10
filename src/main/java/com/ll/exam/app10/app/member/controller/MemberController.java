package com.ll.exam.app10.app.member.controller;


import com.ll.exam.app10.app.member.Service.MemberService;
import com.ll.exam.app10.app.member.entity.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;


@Controller
@RequestMapping("/member")
@RequiredArgsConstructor
public class MemberController {
    private final MemberService memberService;
    @GetMapping("/join")
    public String showJoin(){
        return "/member/join";
    }

    @PostMapping("/join")
    @ResponseBody
    public String join(String username, String password, String email, MultipartFile profileImg){
       Member oldmember = memberService.getMemberByUserName(username);
        if(oldmember != null){
            return "이미 가입된 회원입니다.";
        }
        Member member = memberService.join(username, "{noop}" + password, email, profileImg);

        return "회원가입 완료";
    }
}
