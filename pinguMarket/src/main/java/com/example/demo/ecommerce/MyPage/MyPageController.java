package com.example.demo.ecommerce.MyPage;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import org.aspectj.weaver.patterns.ThrowsPattern;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;


import com.example.demo.ecommerce.Authuser.Authuser;
import com.example.demo.ecommerce.Coupon.CouponService;
import com.example.demo.ecommerce.Email.EmailService;
import com.example.demo.ecommerce.Entity.Coupon;
import com.example.demo.ecommerce.Entity.Payment;
import com.example.demo.ecommerce.Entity.User;
import com.example.demo.ecommerce.LoginCheck.LoginCheck;
import com.example.demo.ecommerce.Payment.PaymentService;
import com.example.demo.ecommerce.Review.CanNotFoundException;
import com.example.demo.ecommerce.User.EditPwForm;
import com.example.demo.ecommerce.User.UserModifyForm;
import com.example.demo.ecommerce.User.UserService;
import com.example.demo.lms.service.LmsCouponService;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import net.bytebuddy.implementation.bytecode.Throw;


@RequiredArgsConstructor
@Controller
public class MyPageController {
	
	private final UserService us;
	private final PaymentService ps;
	private final CouponService cs;
	private final LmsCouponService lcs;
	private final EmailService es;

	@LoginCheck
	@GetMapping("/myorder")
	public String myOrderPage(@Authuser User user, Model model) throws CanNotFoundException {
//		User u = this.us.getUser(principal.getName());
		User u = this.us.getUser(user.getId());
		// 임시용 user 1
		try {
			List<Payment> payment = this.ps.getPayment(u.getUserId());
//			해당 사용자의 userId로 payment 테이블을 조회
			
			List<String> productNames = payment.stream()
		            .map(ps::getFirstProductName)
		            .toList();
//			getPayment로 조회한 리스트를 바탕으로 첫 상품의 이름을 가져옴
			
			List<Integer> totalPrice = payment.stream()
					.map(ps::getTotalPrice)
					.toList();
//			getPayment로 조회한 리스트를 바탕으로 해당 결제의 총 가격을 가져옴
			
			model.addAttribute("user", u);
			model.addAttribute("paymentList", payment);
			model.addAttribute("productNames", productNames);
			model.addAttribute("totalprices", totalPrice);
			return "Mypage/myorder";
		} catch (CanNotFoundException e) {
			// TODO: handle exception
			return "redirect:/main";
		}
	
		
	}
	
	@LoginCheck
	@PostMapping("periodloading")
	@ResponseBody
	public ResponseEntity<MyOrderResponseDTO> periodLoading(@RequestParam("period") Integer period,
			@Authuser User user) throws CanNotFoundException {
	    User u = this.us.getUser(user.getId());
	   
	    List<Payment> payments = this.ps.getPaymentPeriod(u.getUserId(), period);
//		user_id와 전송받은 period 파라미터를 이용해서 조회
//	    이거 해당 기간에 상품내역 없으면 비동기로 예외 뜨니까 메세지 보내주면 좋을 것 같음
	    
	    List<String> productNames = payments.stream()
	            .map(ps::getFirstProductName)
	            .toList();
//		getPaymentPeriod로 조회한 리스트를 바탕으로 첫 상품의 이름을 가져옴
	    
	    List<Integer> totalPrices = payments.stream()
	            .map(ps::getTotalPrice)
	            .toList();
//		getPaymentPeriod로 조회한 리스트를 바탕으로 해당 결제의 총 가격을 가져옴
	    MyOrderResponseDTO responseDTO = new MyOrderResponseDTO(payments, productNames, totalPrices);
//		responseDTO에 각 값들을 저장
	    System.out.println(responseDTO);
	    return ResponseEntity.ok(responseDTO);
//		문제 없으면 responseDTO 반환
	}
	

	@LoginCheck
	@GetMapping("/myorder/detail/{paymentId}")
	public String myOrderDetailPage(Model model,
			@PathVariable ("paymentId") Integer id, @Authuser User user) throws CanNotFoundException {
//		User u = this.us.getUser(principal.getName());
		User u = this.us.getUser(user.getId());
		Payment p = this.ps.getPayment1(id);
		
		model.addAttribute("user", u);	
		model.addAttribute("payment", p);
		
		if(u.getUserId().equals(p.getUser().getUserId())) {
			return "Mypage/myorderdetail";
		}
//		해당 payment 정보를 가져옴
//		
		return "redirect:/myorder";
		
	}
	
	@LoginCheck
	@GetMapping("/myreview")
	public String myReviewPage(Model model,
			@Authuser User user) throws CanNotFoundException {
//		User u = this.us.getUser(principal.getName());
		User u = this.us.getUser(user.getId());
	
		model.addAttribute("user", u);		
		return "Mypage/myreview";
	}

	@GetMapping("myPage")
	public String myPage(Model model) throws CanNotFoundException {
		
		User u = this.us.getUser(1);
		model.addAttribute("user", u);
		return "Mypage/myPage";
	}
//	테스트용 페이지이므로 나중에 삭제
	

	@PostMapping("/pwcheck")
	@ResponseBody
	public Map<String, Boolean> pwCheck(@RequestParam("password") String password,
			@Authuser User user, HttpSession session) throws CanNotFoundException {
	    User u = this.us.getUser(user.getId());
	    System.out.println(u.getUserId());
	    
	    boolean pwChecked = this.us.pwCheck(password, u.getPw());
//	    pwCheck 메서드를 활용해 pwChecked에 true false 값 저장
	    
	    Map<String, Boolean> data = new HashMap<>();
//	    Map 형식 인스턴스 data 생성
	    
	    data.put("pwChecked", pwChecked);
//	    data에 key pwChecked, 밸류 true, false를 저장
	    
	    return data;
//	    data 반환
	}
	
	@LoginCheck
	@GetMapping("/usermodify") 
	public String myInfoModifyPage(
			Model model, @Authuser User user) throws CanNotFoundException  {
//		User user = this.us.getUser(principal.getName());
//		로그인 생기면 위에걸로 수정
		
		User u = this.us.getUser(user.getId());
		
		UserModifyForm form = new UserModifyForm();
	    form.setEmail1(user.getEmail().split("@")[0]);
	    form.setEmail2(user.getEmail().split("@")[1]);
	    form.setAddress1(user.getAddress().split(" ")[0]);
	    form.setAddress2(user.getAddress().split(" ", 2)[1]);
	    form.setAddressDetail(user.getAddressDetail());
	    form.setGender(user.getGender());
	    form.setTell(user.getTell());
//	    form에 현재 정보를 바로 가져오기 위한 코드
		
	    model.addAttribute("userModifyForm", form);
	    model.addAttribute("user", u);
		
		return "Mypage/usermodify";
	}

	@LoginCheck
	@PostMapping("/usermodify") 
	public String myInfoModifyPage(
			@Valid UserModifyForm userModifyForm, BindingResult bindingResult,
			Model model, @Authuser User user) throws CanNotFoundException  {
		
		
//		User user = this.us.getUser(principal.getName());
//		로그인 생기면 위에걸로 수정
		User u = this.us.getUser(user.getId());
		
	    model.addAttribute("user", user);
	    
		if(bindingResult.hasErrors()) {
			return "Mypage/usermodify";
		}

		
		this.us.userModify(user,
				userModifyForm.getEmail1(),
				userModifyForm.getEmail2(),
				userModifyForm.getAddress1(),
				userModifyForm.getAddress2(),
				userModifyForm.getAddressDetail(),
				userModifyForm.getGender(),
				userModifyForm.getTell());
		
		return "Mypage/usermodify";
		
	}
	
	@PostMapping("/sendcode")
	public ResponseEntity<Map<String, String>> emailAuth(@RequestParam("email") String email) {
		Map<String, String> response = new HashMap<>();
		try {
			String code = es.sendCode(email);
//			메일을 전송하고 인증코드를 저장
			response.put("code", code);
//			인증코드를 response map에 저장
			return ResponseEntity.ok(response);
//			문제없이 코드가 작동했을 경우 response map을 반환
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			response.put("error", "에러남");
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
		}
	}
	
	@LoginCheck
	@GetMapping("/mypage/editpw")
	public String myPwModifyPage(EditPwForm editPwForm,
			Model model, @Authuser User user) throws CanNotFoundException {
			
		User u = this.us.getUser(user.getId());
		
		model.addAttribute("editPwForm", editPwForm);
		model.addAttribute("user", u);
		
			return "mypage/MypageEditPw";
		}
	
	@LoginCheck
	@PostMapping("/mypage/editpw")
	public String myPwModifyPage(Model model, @Authuser User user,
			@Valid EditPwForm editPwForm, BindingResult bindingResult) throws CanNotFoundException {
		User u = this.us.getUser(user.getId());
		model.addAttribute("user", u);
		
		if(us.prePasswordCheck(user, editPwForm.getPrePassword())) {
            //System.out.println("비밀번호 같아용");
            if(!editPwForm.getPassword().equals(editPwForm.getPassword2())) {
                bindingResult.rejectValue("Password2","NoSame", "비밀번호가 일치하지 않습니다");
//                System.out.println("비밀번호안일치");
                return "mypage/MypageEditPw";
            }else {
                if(us.prePasswordCheck(user, editPwForm.getPassword())) {
                    bindingResult.rejectValue("Password","SameError", "이미 사용중인 비밀번호 입니다.");
//                    System.out.println("이미사용중");
                    return "mypage/MypageEditPw";
                }
            }
            this.us.changePassword(user, editPwForm.getPassword());
//            System.out.println("변경완료");
            return "mypage/MypageEditPw";
        }else {
            bindingResult.rejectValue("prePassword","NoSame", "현재 비밀번호가 일치하지 않습니다");
//            System.out.println("현재 비밀번호가 틀림");
            //System.out.println("비밀번호 틀려용");
        }
        return "mypage/MypageEditPw";
    }
	
	
	@LoginCheck
	@GetMapping("/mycoupon")
	public String myCouponPage(Model model, @Authuser User user) throws CanNotFoundException {
		
		User u = this.us.getUser(user.getId());
		List<Coupon> coupon = this.cs.getCoupon(user.getUserId());
//		userId로 쿠폰을 조회하되 useYn 값이 n인 경우만 가져옴
		
		model.addAttribute("user", u);
		model.addAttribute("couponList", coupon);
		return "Mypage/mycoupon";
	}
	
	@LoginCheck
	@PostMapping("/mycoupon/inputcoupon")
	public ResponseEntity<String> useCoupon(@RequestParam("code")String code)
			throws CanNotFoundException,CouponOverlappingException {
		
		if(lcs.existCoupon(code) && !lcs.useCheck(code)) {
			cs.createCoupon(code);
			lcs.useCoupon(code);
			System.out.println(lcs.existCoupon(code)+","+lcs.useCheck(code));
			return ResponseEntity.ok("쿠폰 정상 입력");
		}
		else {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("쿠폰에러");
		}
	}
	
	@LoginCheck
	@GetMapping("/signout")
	public String singoutPage(Model model, @Authuser User user) throws CanNotFoundException {
		User u = this.us.getUser(user.getId());
		
		model.addAttribute("user", u);

		return "Mypage/signout";
	}
	
	@LoginCheck
	@PostMapping("/signout") 
	public String signoutPage(@Authuser User user) throws CanNotFoundException {
		
		User u = this.us.getUser(user.getId());
		
		us.userSignout(u, "y");
//		해당 사용자의 signoutYn 컬럼을 y로 변경
		
//		return "redirect:/user/logout";
//		로그인 기능이 생길 경우 logout 페이지로 보내서 바로 로그아웃시켜야 함
		return "redirect:/myorder";
	}

}
