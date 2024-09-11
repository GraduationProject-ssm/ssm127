package com.controller;

import java.text.SimpleDateFormat;
import java.util.*;
import javax.servlet.http.HttpServletRequest;

import com.annotation.IgnoreAuth;
import com.entity.QiuduiEntity;
import com.service.QiuduiService;
import com.service.TokenService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.baomidou.mybatisplus.mapper.Wrapper;

import com.entity.YonghuEntity;

import com.service.YonghuService;
import com.utils.PageUtils;
import com.utils.R;

/**
 * 球员信息
 * 后端接口
 * @author
 * @email
 * @date 2021-03-22
*/
@RestController
@Controller
@RequestMapping("/yonghu")
public class YonghuController {
    private static final Logger logger = LoggerFactory.getLogger(YonghuController.class);

    @Autowired
    private QiuduiService qiuduiService;

    @Autowired
    private YonghuService yonghuService;

    @Autowired
    private TokenService tokenService;

    /**
     * 登录
     */
    @IgnoreAuth
    @RequestMapping(value = "/login")
    public R login(String username, String password, String role, HttpServletRequest request) {
        YonghuEntity user = yonghuService.selectOne(new EntityWrapper<YonghuEntity>().eq("username", username));
        if(user != null){
            if(!user.getRole().equals(role)){
                return R.error("权限不正常");
            }
            if(user==null || !user.getPassword().equals(password)) {
                return R.error("账号或密码不正确");
            }
            String token = tokenService.generateToken(user.getId(),user.getName(), "users", user.getRole());
            return R.ok().put("token", token);
        }else{
            return R.error("账号或密码或权限不对");
        }

    }

    /**
     * 注册
     */
    @IgnoreAuth
    @PostMapping(value = "/register")
    public R register(@RequestBody YonghuEntity user){
//    	ValidatorUtils.validateEntity(user);
        if(yonghuService.selectOne(new EntityWrapper<YonghuEntity>().eq("username", user.getUsername())) !=null) {
            return R.error("球员已存在");
        }
        yonghuService.insert(user);
        return R.ok();
    }

    /**
     * 退出
     */
    @GetMapping(value = "logout")
    public R logout(HttpServletRequest request) {
        request.getSession().invalidate();
        return R.ok("退出成功");
    }

    /**
     * 密码重置
     */
    @IgnoreAuth
    @RequestMapping(value = "/resetPass")
    public R resetPass(String username, HttpServletRequest request){
        YonghuEntity user = yonghuService.selectOne(new EntityWrapper<YonghuEntity>().eq("username", username));
        if(user==null) {
            return R.error("账号不存在");
        }
        user.setPassword("123456");
        yonghuService.update(user,null);
        return R.ok("密码已重置为：123456");
    }

    /**
     * 获取球员的session球员信息
     */
    @RequestMapping("/session")
    public R getCurrUser(HttpServletRequest request){
        Integer id = (Integer)request.getSession().getAttribute("userId");
        YonghuEntity user = yonghuService.selectById(id);
        return R.ok().put("data", user);
    }


    /**
    * 后端列表
    */
    @RequestMapping("/page")
    public R page(@RequestParam Map<String, Object> params){
        logger.debug("Controller:"+this.getClass().getName()+",page方法");
        PageUtils page = yonghuService.queryPage(params);
        return R.ok().put("data", page);
    }
    /**
    * 后端详情
    */
    @RequestMapping("/info/{id}")
    public R info(@PathVariable("id") Long id){
        logger.debug("Controller:"+this.getClass().getName()+",info方法");
        YonghuEntity yonghu = yonghuService.selectById(id);
        if(yonghu!=null){
            return R.ok().put("data", yonghu);
        }else {
            return R.error(511,"查不到数据");
        }

    }

    /**
    * 后端保存
    */
    @RequestMapping("/save")
    public R save(@RequestBody YonghuEntity yonghu, HttpServletRequest request){
        logger.debug("Controller:"+this.getClass().getName()+",save");
        Wrapper<YonghuEntity> queryWrapper = new EntityWrapper<YonghuEntity>()
            .eq("name", yonghu.getName())
            .eq("username", yonghu.getUsername())
            ;
        logger.info("sql语句:"+queryWrapper.getSqlSegment());
        YonghuEntity yonghuEntity = yonghuService.selectOne(queryWrapper);
        if("".equals(yonghu.getImgPhoto()) || "null".equals(yonghu.getImgPhoto())){
            yonghu.setImgPhoto(null);
        }
        QiuduiEntity qiudui = qiuduiService.selectById(yonghu.getQdTypes());
        if(qiudui == null){
            return R.error();
        }
        qiudui.setSum(qiudui.getSum()+1);
        if(yonghuEntity==null){
            yonghu.setPassword("123456");
            yonghu.setRole("球员");
            yonghuService.insert(yonghu);
            qiuduiService.updateById(qiudui);
            return R.ok();
        }else {
            return R.error(511,"表中有相同数据");
        }
    }

    /**
    * 修改
    */
    @RequestMapping("/update")
    public R update(@RequestBody YonghuEntity yonghu, HttpServletRequest request){
        logger.debug("Controller:"+this.getClass().getName()+",update");
        //根据字段查询是否有相同数据
        Wrapper<YonghuEntity> queryWrapper = new EntityWrapper<YonghuEntity>()
            .notIn("id",yonghu.getId())
            .eq("name", yonghu.getName())
            .eq("username", yonghu.getUsername())
            .eq("password", yonghu.getPassword())
            .eq("jiguan", yonghu.getJiguan())
            .eq("age", yonghu.getAge())
            .eq("height", yonghu.getHeight())
            .eq("averaged", yonghu.getAveraged())
            .eq("backboard", yonghu.getBackboard())
            .eq("Assists", yonghu.getAssists())
            .eq("sex_types", yonghu.getSexTypes())
            .eq("qd_types", yonghu.getQdTypes())
            .eq("phone", yonghu.getPhone())
            .eq("role", yonghu.getRole())
            ;
        logger.info("sql语句:"+queryWrapper.getSqlSegment());
        YonghuEntity yonghuEntity = yonghuService.selectOne(queryWrapper);
        if("".equals(yonghu.getImgPhoto()) || "null".equals(yonghu.getImgPhoto())){
                yonghu.setImgPhoto(null);
        }
        if(yonghuEntity==null){
            yonghuService.updateById(yonghu);//根据id更新
            return R.ok();
        }else {
            return R.error(511,"表中有相同数据");
        }
    }


    /**
    * 删除
    */
    @RequestMapping("/delete")
    public R delete(Integer ids){
        YonghuEntity yonghu = yonghuService.selectById(ids);
        if(yonghu == null){
            return R.error();
        }
        QiuduiEntity qiudui = qiuduiService.selectById(yonghu.getQdTypes());
        if(qiudui == null){
            return R.error();
        }
        qiudui.setSum(qiudui.getSum()-1);
        yonghuService.deleteById(ids);
        qiuduiService.updateById(qiudui);
        return R.ok();
    }
}

