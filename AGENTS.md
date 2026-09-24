# Orientações para agentes

Estas regras se aplicam a qualquer agente, ferramenta ou modelo que trabalhe neste repositório.

## Fluxo obrigatório para mudanças

1. Antes de alterar código para uma correção, melhoria, nova função ou remediação de segurança, procure uma Issue aberta que cubra o trabalho. Se não existir, crie uma Issue no GitHub com objetivo, escopo e critérios de aceite. Não comece a implementação até a Issue estar registrada.
2. Crie uma branch dedicada a partir da branch de destino atual. Use o padrão `codex/issue-<número>-<descrição-curta>`.
3. Mantenha a mudança focada no escopo da Issue. Se o trabalho revelar outra tarefa independente, abra outra Issue e vincule-a.
4. Abra um Pull Request para cada Issue sempre que as mudanças puderem ser revisadas e entregues de forma independente. Quando um único PR cobrir mais de uma Issue, mencione todas no corpo e explique a relação entre elas.
5. O corpo de todo PR deve mencionar a Issue, incluir resumo, validações executadas e riscos ou limitações conhecidos. Use `Closes #<número>` quando o PR concluir integralmente a Issue; use `Refs #<número>` quando apenas contribuir para ela.
6. Não faça commit direto na branch principal nem publique uma implantação manual a partir da máquina local. Integração e implantação devem passar pelo Pull Request e pelos controles de CI configurados no repositório.

## Evidência e segurança

- Não declare testes, verificações ou correções que não tenham sido executados ou confirmados.
- Em auditorias, separe fatos observados de inferências e inclua evidência local e fontes para afirmações externas.
- Se um dado necessário não puder ser verificado no código ou em uma fonte confiável, registre a limitação e pergunte ao responsável antes de afirmar uma conclusão.
