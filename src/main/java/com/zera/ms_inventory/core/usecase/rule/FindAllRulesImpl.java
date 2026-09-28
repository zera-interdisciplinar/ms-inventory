package com.zera.ms_inventory.core.usecase.rule;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.zera.ms_inventory.core.domain.entity.Rule;
import com.zera.ms_inventory.core.domain.valueobject.DefaultRules;
import com.zera.ms_inventory.core.repository.RuleRepository;

@Service
public class FindAllRulesImpl implements FindAllRules {

    private final RuleRepository ruleRepository;

    public FindAllRulesImpl(RuleRepository ruleRepository) {
        this.ruleRepository = ruleRepository;
    }

    /**
     * Primeiro acesso da unidade cria as regras padrao, para o alerta funcionar sem ninguem
     * configurar nada. So semeia quando nao existe regra nenhuma: quem apagou todas de proposito
     * nao quer ve-las de volta na proxima listagem, mas isso ja e uma unidade que passou por aqui.
     */
    @Override
    @Transactional
    public List<Rule> execute(UUID unitId) {
        if (ruleRepository.countByUnit(unitId) == 0) {
            return ruleRepository.saveAll(DefaultRules.forUnit(unitId));
        }
        return ruleRepository.findAll(unitId);
    }
}
