package com.ghhh.qmmc.activity.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ghhh.qmmc.activity.service.ActivityInfoService;
import com.ghhh.qmmc.model.activity.ActivityInfo;
import com.ghhh.qmmc.result.Result;
import com.ghhh.qmmc.vo.activity.ActivityRuleVo;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin/activity/activityInfo")
public class ActivityInfoController {

    @Autowired
    private ActivityInfoService activityInfoService;

//    url: `${api_name}/${page}/${limit}`,
//    method: 'get'
    @ApiOperation("分页查询列表")
    @GetMapping("/{page}/{limit}")
    public Result getPage(@PathVariable Long page,
                          @PathVariable Long limit){
        Page<ActivityInfo> infoPage = new Page<>(page, limit);
        IPage<ActivityInfo> pageModel = activityInfoService.getPage(infoPage);
        return Result.ok(pageModel);
    }

//    url: `${api_name}/save`,
//    method: 'post',
//    data: role
    @ApiOperation("增加")
    @PostMapping("/save")
    public Result save(@RequestBody ActivityInfo activityInfo){
        activityInfoService.save(activityInfo);
        return Result.ok(null);
    }

    @ApiOperation(value = "获取活动")
    @GetMapping("get/{id}")
    public Result get(@PathVariable Long id) {
        ActivityInfo activityInfo = activityInfoService.getById(id);
        activityInfo.setActivityTypeString(activityInfo.getActivityType().getComment());
        return Result.ok(activityInfo);
    }

    @ApiOperation(value = "修改活动")
    @PutMapping("update")
    public Result updateById(@RequestBody ActivityInfo activityInfo) {
        activityInfoService.updateById(activityInfo);
        return Result.ok(null);
    }

    @ApiOperation(value = "删除活动")
    @DeleteMapping("remove/{id}")
    public Result remove(@PathVariable Long id) {
        activityInfoService.removeById(id);
        return Result.ok(null);
    }

    @ApiOperation(value="根据id列表删除活动")
    @DeleteMapping("batchRemove")
    public Result batchRemove(@RequestBody List<String> idList){
        activityInfoService.removeByIds(idList);
        return Result.ok(null);
    }

//    url: `${api_name}/findActivityRuleList/${id}`,
////    method: 'get'
    @GetMapping("/findActivityRuleList/{id}")
    public Result findActivityRuleList(@PathVariable Long id){
       Map<String,Object> map =  activityInfoService.findActivityRuleList(id);
       return Result.ok(map);
    }

    @ApiOperation(value = "新增活动规则")
    @PostMapping("saveActivityRule")
    public Result saveActivityRule(@RequestBody ActivityRuleVo activityRuleVo) {
        activityInfoService.saveActivityRule(activityRuleVo);
        return Result.ok(null);
    }

    /**
     * 根据关键字获取sku列表，活动使用
     * @param keyword
     * @return
     */
    @GetMapping("findSkuInfoByKeyword/{keyword}")
    public Result findSkuInfoByKeyword(@PathVariable("keyword") String keyword) {
        return Result.ok(activityInfoService.findSkuInfoByKeyword(keyword));
    }



}
