package com.ll.exam.app10;

import com.ll.exam.app10.app.home.controller.HomeController;
import com.ll.exam.app10.app.member.Service.MemberService;
import com.ll.exam.app10.app.member.controller.MemberController;
import com.ll.exam.app10.app.member.entity.Member;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.web.client.RestTemplate;

import javax.transaction.Transactional;

import java.io.InputStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles({"base-addi", "test"})
class AppTest {

	@Autowired
	private MockMvc mvc;

	@Autowired
	private  MemberService memberService;

	@Test
	@DisplayName("메인에서 안녕이 나오게")
	void ti() throws Exception{

		ResultActions resultActions = mvc
				.perform(get("/"))
				.andDo(print());

		resultActions
				.andExpect(status().is2xxSuccessful())
				.andExpect(handler().handlerType(HomeController.class))
				.andExpect(handler().methodName("main"))
				.andExpect(content().string(containsString("안녕")));
	}

	@Test
	@DisplayName("회원의 수")
	@Rollback(false)
	void t2() throws Exception {
		long count = memberService.count();
		assertThat(count).isGreaterThan(0);
	}
	@Test
	@DisplayName("user1로 로그인 후 프로필페이지에 접속하면 user1의 이메일이 보여야 한다.")
	@Rollback(false)
	void t3() throws Exception {
		// mockMvc로 로그인 처리
		// WHEN
		// GET /
		ResultActions resultActions = mvc
				.perform(
						get("/member/profile")
								.with(user("user1").password("1234").roles("user"))
				)
				.andDo(print());

		// THEN
		// 안녕
		resultActions
				.andExpect(status().is2xxSuccessful())
				.andExpect(handler().handlerType(MemberController.class))
				.andExpect(handler().methodName("showProfile"))
				.andExpect(content().string(containsString("user1@test.com")));
	}

	@Test
	@DisplayName("user4로 로그인 후 프로필페이지에 접속하면 user4의 이메일이 보여야 한다.")
	@Rollback(false)
	void t4() throws Exception {

		ResultActions resultActions = mvc
				.perform(
						get("/member/profile")
								.with(user("user4").password("1234").roles("user"))
				)
				.andDo(print());

		resultActions
				.andExpect(status().is2xxSuccessful())
				.andExpect(handler().handlerType(MemberController.class))
				.andExpect(handler().methodName("showProfile"))
				.andExpect(content().string(containsString("user4@test.com")));
	}

	@Test
	@DisplayName("회원가입")
	void t5() throws Exception {
		String testUploadFileUrl = "https://picsum.photos/200/300";
		String originalFileName = "test.png";

		// wget
		RestTemplate restTemplate = new RestTemplate();
		ResponseEntity<Resource> response = restTemplate.getForEntity(testUploadFileUrl, Resource.class);
		InputStream inputStream = response.getBody().getInputStream();

		MockMultipartFile profileImg = new MockMultipartFile(
				"profileImg",   // 저장 이름? UUID + 1.png 했던 그런건가?
				originalFileName,
				"image/png",
				inputStream    // 이미지 ?
		);

		// 회원가입(MVC MOCK)
		// when
		ResultActions resultActions = mvc.perform(
						multipart("/member/join")
								.file(profileImg)
								.param("username", "user5")
								.param("password", "1234")
								.param("email", "user5@test.com")
								.characterEncoding("UTF-8"))
				.andDo(print());

		resultActions
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/member/profile"))
				.andExpect(handler().handlerType(MemberController.class))
				.andExpect(handler().methodName("join"));

		Member member = memberService.getMemberById(5L);

		assertThat(member).isNotNull();

		// 5번회원의 프로필 이미지 제거
		memberService.removeProfileImg(member);
	}
}
