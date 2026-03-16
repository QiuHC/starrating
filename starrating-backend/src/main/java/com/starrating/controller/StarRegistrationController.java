package com.starrating.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.starrating.entity.StarRegistration;
import com.starrating.service.StarRegistrationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/registrations")
@CrossOrigin
public class StarRegistrationController {

    @Autowired
    private StarRegistrationService starRegistrationService;

    // 提交报名申请
    @PostMapping("/submit")
    public String submit(@RequestBody StarRegistration registration) {
        registration.setStatus("PENDING");
        registration.setSubmitTime(OffsetDateTime.now());
        registration.setCreateTime(OffsetDateTime.now());
        registration.setUpdateTime(OffsetDateTime.now());
        starRegistrationService.save(registration);
        return "提交成功";
    }

    // 获取申报列表 (如果是服务店，可传入 shopCode 进行过滤)
    @GetMapping("/list")
    public List<StarRegistration> list(@RequestParam(required = false) String shopCode) {
        QueryWrapper<StarRegistration> queryWrapper = new QueryWrapper<>();
        if (shopCode != null && !shopCode.isEmpty()) {
            queryWrapper.eq("shop_code", shopCode);
        }
        queryWrapper.orderByDesc("create_time");
        return starRegistrationService.list(queryWrapper);
    }

    // 审核通过
    @PostMapping("/{id}/approve")
    public String approve(@PathVariable Long id) {
        StarRegistration reg = starRegistrationService.getById(id);
        if (reg != null) {
            reg.setStatus("APPROVED");
            reg.setAuditTime(OffsetDateTime.now());
            reg.setUpdateTime(OffsetDateTime.now());
            starRegistrationService.updateById(reg);
            return "审核通过";
        }
        return "记录不存在";
    }

    // 审核驳回
    @PostMapping("/{id}/reject")
    public String reject(@PathVariable Long id, @RequestBody Map<String, String> payload) {
        StarRegistration reg = starRegistrationService.getById(id);
        if (reg != null) {
            reg.setStatus("REJECTED");
            reg.setRejectReason(payload.get("reason"));
            reg.setAuditTime(OffsetDateTime.now());
            reg.setUpdateTime(OffsetDateTime.now());
            starRegistrationService.updateById(reg);
            return "已驳回";
        }
        return "记录不存在";
    }
}
