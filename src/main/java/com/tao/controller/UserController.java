package com.tao.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tao.common.R;
import com.tao.pojo.User;
import com.tao.service.UserService;
import com.tao.utils.SMSUtils;
import com.tao.utils.ValidateCodeUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/user")
@Slf4j
public class UserController {

    @Autowired
    private UserService userService;
    @Autowired
    private RedisTemplate redisTemplate;


    //发送手机短信验证码
    @PostMapping("/sendMsg")
    public R<String> sendMsg(@RequestBody  User user, HttpSession session){
        //获取手机号
        String phone = user.getPhone();

        if(StringUtils.hasText(phone)){

            //生成随机四位验证码
            String code = ValidateCodeUtils.generateValidateCode(4).toString();
            log.info("code={}",code);

            //调用阿里云短信服务
            //SMSUtils.sendMessage("外卖","",phone,code);

            //需要将生成的验证码保存到session
            //session.setAttribute(phone,code);

            //将生成的验证码缓存到Redis中，并设置有效期5分钟
            redisTemplate.opsForValue().set(phone,code,5,TimeUnit.MINUTES);

            return R.success("手机短信验证码发送成功");

        }

        return R.error("短信发送失败");
    }

    //移动端用户登录
    @PostMapping("/login")
    public R<User> login(@RequestBody Map map, HttpSession session){

        //获取手机号
        String phone = map.get("phone").toString();

        //获取验证码
        String code = map.get("code").toString();

        //从session获取保存的验证码
        //Object codeInSession = session.getAttribute(phone);

        //从Redis中获取保存的验证码
        Object codeInSession = redisTemplate.opsForValue().get(phone);

        //验证码比对，页面提交的和色素死哦你中的
        if(codeInSession !=null && codeInSession.equals(code)){
            //比对成功则登录成功

            LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(User::getPhone,phone);

            User user = userService.getOne(queryWrapper);
            //判断用户是否为新用户，如果是则完成注册
            if(user == null){
                user = new User();
                user.setPhone(phone);
                user.setStatus(1);
                userService.save(user);
            }
            session.setAttribute("user",user.getId());

            //如果用户登录成功，删除redis中缓存的验证码
            redisTemplate.delete(phone);

            return R.success(user);

        }

        return R.error("登录失败");
    }
}
