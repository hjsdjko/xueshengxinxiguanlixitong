package com.controller;


import java.text.SimpleDateFormat;
import com.alibaba.fastjson.JSONObject;
import java.util.*;
import org.springframework.beans.BeanUtils;
import javax.servlet.http.HttpServletRequest;
import org.springframework.web.context.ContextLoader;
import javax.servlet.ServletContext;
import com.service.TokenService;
import com.utils.StringUtil;
import java.lang.reflect.InvocationTargetException;

import com.service.DictionaryService;
import org.apache.commons.lang3.StringUtils;
import com.annotation.IgnoreAuth;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.baomidou.mybatisplus.mapper.Wrapper;

import com.entity.ZhengshuEntity;

import com.service.ZhengshuService;
import com.entity.view.ZhengshuView;
import com.service.YonghuService;
import com.entity.YonghuEntity;
import com.utils.PageUtils;
import com.utils.R;

/**
 * 学生证书
 * 后端接口
 * @author
 * @email
 * @date 2021-04-05
*/
@RestController
@Controller
@RequestMapping("/zhengshu")
public class ZhengshuController {
    private static final Logger logger = LoggerFactory.getLogger(ZhengshuController.class);

    @Autowired
    private ZhengshuService zhengshuService;


    @Autowired
    private TokenService tokenService;
    @Autowired
    private DictionaryService dictionaryService;


    //级联表service
    @Autowired
    private YonghuService yonghuService;


    /**
    * 后端列表
    */
    @RequestMapping("/page")
    public R page(@RequestParam Map<String, Object> params, HttpServletRequest request){
        logger.debug("page方法:,,Controller:{},,params:{}",this.getClass().getName(),JSONObject.toJSONString(params));
        String role = String.valueOf(request.getSession().getAttribute("role"));
        if(StringUtil.isNotEmpty(role) && "用户".equals(role)){
            params.put("yonghuId",request.getSession().getAttribute("userId"));
        }
        params.put("orderBy","id");
        PageUtils page = zhengshuService.queryPage(params);

        //字典表数据转换
        List<ZhengshuView> list =(List<ZhengshuView>)page.getList();
        for(ZhengshuView c:list){
            //修改对应字典表字段
            dictionaryService.dictionaryConvert(c);
        }
        return R.ok().put("data", page);
    }

    /**
    * 后端详情
    */
    @RequestMapping("/info/{id}")
    public R info(@PathVariable("id") Long id){
        logger.debug("info方法:,,Controller:{},,id:{}",this.getClass().getName(),id);
        ZhengshuEntity zhengshu = zhengshuService.selectById(id);
        if(zhengshu !=null){
            //entity转view
            ZhengshuView view = new ZhengshuView();
            BeanUtils.copyProperties( zhengshu , view );//把实体数据重构到view中

            //级联表
            YonghuEntity yonghu = yonghuService.selectById(zhengshu.getYonghuId());
            if(yonghu != null){
                BeanUtils.copyProperties( yonghu , view ,new String[]{ "id", "createDate"});//把级联的数据添加到view中,并排除id和创建时间字段
                view.setYonghuId(yonghu.getId());
            }
            //修改对应字典表字段
            dictionaryService.dictionaryConvert(view);
            return R.ok().put("data", view);
        }else {
            return R.error(511,"查不到数据");
        }

    }

    /**
    * 后端保存
    */
    @RequestMapping("/save")
    public R save(@RequestBody ZhengshuEntity zhengshu, HttpServletRequest request){
        logger.debug("save方法:,,Controller:{},,zhengshu:{}",this.getClass().getName(),zhengshu.toString());
        Wrapper<ZhengshuEntity> queryWrapper = new EntityWrapper<ZhengshuEntity>()
            .eq("yonghu_id", zhengshu.getYonghuId())
            .eq("zhengshu_name", zhengshu.getZhengshuName())
            .eq("zhengshu_types", zhengshu.getZhengshuTypes())
            ;
        logger.info("sql语句:"+queryWrapper.getSqlSegment());
        ZhengshuEntity zhengshuEntity = zhengshuService.selectOne(queryWrapper);
        if(zhengshuEntity==null){
            zhengshu.setCreateTime(new Date());
        //  String role = String.valueOf(request.getSession().getAttribute("role"));
        //  if("".equals(role)){
        //      zhengshu.set
        //  }
            zhengshuService.insert(zhengshu);
            return R.ok();
        }else {
            return R.error(511,"表中有相同数据");
        }
    }

    /**
    * 后端修改
    */
    @RequestMapping("/update")
    public R update(@RequestBody ZhengshuEntity zhengshu, HttpServletRequest request){
        logger.debug("update方法:,,Controller:{},,zhengshu:{}",this.getClass().getName(),zhengshu.toString());
        //根据字段查询是否有相同数据
        Wrapper<ZhengshuEntity> queryWrapper = new EntityWrapper<ZhengshuEntity>()
            .notIn("id",zhengshu.getId())
            .andNew()
            .eq("yonghu_id", zhengshu.getYonghuId())
            .eq("zhengshu_name", zhengshu.getZhengshuName())
            .eq("zhengshu_types", zhengshu.getZhengshuTypes())
            ;
        logger.info("sql语句:"+queryWrapper.getSqlSegment());
        ZhengshuEntity zhengshuEntity = zhengshuService.selectOne(queryWrapper);
        if(zhengshuEntity==null){
            //  String role = String.valueOf(request.getSession().getAttribute("role"));
            //  if("".equals(role)){
            //      zhengshu.set
            //  }
            zhengshuService.updateById(zhengshu);//根据id更新
            return R.ok();
        }else {
            return R.error(511,"表中有相同数据");
        }
    }


    /**
    * 删除
    */
    @RequestMapping("/delete")
    public R delete(@RequestBody Integer[] ids){
        logger.debug("delete:,,Controller:{},,ids:{}",this.getClass().getName(),ids.toString());
        zhengshuService.deleteBatchIds(Arrays.asList(ids));
        return R.ok();
    }



}

