package com.starrating.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.starrating.entity.SysConfig;
import com.starrating.service.SysConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/config")
@CrossOrigin
public class SysConfigController {

    @Autowired
    private SysConfigService sysConfigService;

    // 获取系统配置 (如 registration_period)
    @GetMapping("/{key}")
    public SysConfig getConfig(@PathVariable String key) {
        return sysConfigService.getOne(new QueryWrapper<SysConfig>().eq("config_key", key));
    }

    // 更新报名周期设置
    @PostMapping("/period")
    @PreAuthorize("hasAuthority('config_manage')")
    public String updatePeriod(@RequestBody Map<String, String> period) {
        // period payload: { "startTime": "...", "endTime": "..." }
        String jsonValue = String.format("{\"startTime\": \"%s\", \"endTime\": \"%s\"}", 
            period.get("startTime"), period.get("endTime"));
            
        UpdateWrapper<SysConfig> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("config_key", "registration_period")
                     .set("config_value", jsonValue)
                     .set("update_time", OffsetDateTime.now());
                     
        sysConfigService.update(updateWrapper);
        return "更新成功";
    }
}
