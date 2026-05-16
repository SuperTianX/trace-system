package com.steel.trace.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.steel.trace.common.exception.BusinessException;
import com.steel.trace.entity.RuleConfig;
import com.steel.trace.mapper.RuleConfigMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RuleConfigService {

    private final RuleConfigMapper ruleConfigMapper;

    @Transactional
    public RuleConfig create(RuleConfig ruleConfig) {
        if (ruleConfigMapper.selectOne(new LambdaQueryWrapper<RuleConfig>()
                .eq(RuleConfig::getRuleCode, ruleConfig.getRuleCode())) != null) {
            throw new BusinessException("规则编码已存在");
        }
        ruleConfig.setIsEnabled(1);
        ruleConfigMapper.insert(ruleConfig);
        return ruleConfig;
    }

    public RuleConfig getById(Long id) {
        return ruleConfigMapper.selectById(id);
    }

    public Page<RuleConfig> pageQuery(int page, int size, Integer ruleType) {
        LambdaQueryWrapper<RuleConfig> wrapper = new LambdaQueryWrapper<>();
        if (ruleType != null) wrapper.eq(RuleConfig::getRuleType, ruleType);
        wrapper.orderByDesc(RuleConfig::getId);
        return ruleConfigMapper.selectPage(new Page<>(page, size), wrapper);
    }

    @Transactional
    public void toggleEnabled(Long id) {
        RuleConfig config = ruleConfigMapper.selectById(id);
        if (config == null) throw new BusinessException("规则不存在");
        config.setIsEnabled(config.getIsEnabled() == 1 ? 0 : 1);
        ruleConfigMapper.updateById(config);
    }

    @Transactional
    public void delete(Long id) {
        ruleConfigMapper.deleteById(id);
    }
}
