#!/usr/bin/env bash
# PostToolUse(Edit|Write): Java 파일의 컨벤션 위반을 경고합니다. exit 2 → stderr를 Claude에게 피드백.
file=$(jq -r '.tool_input.file_path // empty')
case "$file" in
    *.java) ;;
    *) exit 0 ;;
esac
[ -f "$file" ] || exit 0

warnings=()

grep -nq $'\t' "$file" && warnings+=("탭 들여쓰기 발견 → 스페이스 4칸 사용")
grep -Eq '^[[:space:]]*@Autowired[[:space:]]*$' "$file" && grep -A1 -E '^[[:space:]]*@Autowired' "$file" | grep -Eq 'private[[:space:]]' \
    && warnings+=("@Autowired 필드 주입 → @RequiredArgsConstructor + private final 생성자 주입 사용")
grep -q 'System\.out\.print' "$file" && warnings+=("System.out.print 사용 → @Slf4j 로거 사용")
grep -q '\.printStackTrace()' "$file" && warnings+=("e.printStackTrace() 사용 → log.error(\"...\", e) 사용")
grep -q 'FetchType\.EAGER' "$file" && warnings+=("FetchType.EAGER 사용 → LAZY + fetch join/@EntityGraph 사용")
grep -q 'collect(Collectors\.toList())' "$file" && warnings+=("collect(Collectors.toList()) → Stream.toList() 사용")
grep -Eq '^import[[:space:]]+[a-zA-Z0-9_.]+\.\*;' "$file" && warnings+=("와일드카드 import 사용")
grep -q 'import jakarta\.transaction\.Transactional' "$file" \
    && warnings+=("jakarta.transaction.Transactional → org.springframework.transaction.annotation.Transactional 사용")

if grep -q '@Entity' "$file"; then
    grep -Eq '^[[:space:]]*@(Setter|Data)' "$file" && warnings+=("Entity에 @Setter/@Data 사용 → 도메인 메서드로 상태 변경")
    grep -Eq '@(ManyToOne|OneToOne)[[:space:]]*$|@(ManyToOne|OneToOne)\([^)]*\)' "$file" \
        && grep -E '@(ManyToOne|OneToOne)' "$file" | grep -vq 'FetchType.LAZY' \
        && warnings+=("@ManyToOne/@OneToOne에 fetch = FetchType.LAZY 누락")
    grep -q '@Enumerated' "$file" && grep '@Enumerated' "$file" | grep -vq 'EnumType.STRING' \
        && warnings+=("@Enumerated에 EnumType.STRING 누락")
    grep -q '@ManyToMany' "$file" && warnings+=("@ManyToMany 사용 → 중간 엔티티로 매핑")
fi

case "$file" in
    */controller/*)
        grep -Eq 'Repository[[:space:]]+[a-z]' "$file" && warnings+=("Controller에서 Repository 직접 사용 → Service를 통해 접근")
        grep -q '@Transactional' "$file" && warnings+=("Controller에 @Transactional → Service 계층으로 이동")
        ;;
esac

if [ ${#warnings[@]} -gt 0 ]; then
    echo "⚠️  $(basename "$file") 컨벤션 경고:" >&2
    for w in "${warnings[@]}"; do echo "  - $w" >&2; done
    exit 2
fi
exit 0
