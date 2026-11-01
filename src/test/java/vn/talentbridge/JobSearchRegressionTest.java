package vn.talentbridge;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import vn.talentbridge.adapter.out.persistence.adapter.JobRepositoryAdapter;
import vn.talentbridge.adapter.out.persistence.entity.CompanyJpaEntity;
import vn.talentbridge.adapter.out.persistence.entity.JobJpaEntity;
import vn.talentbridge.adapter.out.persistence.repository.CompanyJpaRepository;
import vn.talentbridge.adapter.out.persistence.repository.JobJpaRepository;
import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest(properties = {"spring.flyway.enabled=false", "spring.jpa.hibernate.ddl-auto=create-drop"}, showSql = false)
@ActiveProfiles("test")
@Import(JobRepositoryAdapter.class)
class JobSearchRegressionTest {
    @Autowired JobRepositoryAdapter search;
    @Autowired JobJpaRepository jobs;
    @Autowired CompanyJpaRepository companies;

    private JobJpaEntity job(String title, boolean negotiable, BigDecimal min, BigDecimal max) {
        var company = CompanyJpaEntity.builder().name("Search QA").build();
        company.setCreatedAt(java.time.LocalDateTime.of(2026, 1, 1, 0, 0));
        company.setUpdatedAt(company.getCreatedAt());
        company = companies.saveAndFlush(company);
        var entity = JobJpaEntity.builder().company(company).title(title)
            .description("Isolated regression fixture").minSalary(min).maxSalary(max)
            .isNegotiable(negotiable).build();
        entity.setCreatedAt(company.getCreatedAt());
        entity.setUpdatedAt(company.getCreatedAt());
        return jobs.saveAndFlush(entity);
    }

    @Test void salaryFiltersExcludeNegotiableAndUnknownPayButUnfilteredSearchKeepsThem() {
        job("Negotiable", true, new BigDecimal("15000000"), new BigDecimal("30000000"));
        job("Unknown", false, null, null);
        var disclosed = job("Disclosed", false, new BigDecimal("15000000"), new BigDecimal("30000000"));
        for (var range : new BigDecimal[][] {
                {new BigDecimal("15000000"), null}, {null, new BigDecimal("30000000")},
                {new BigDecimal("15000000"), new BigDecimal("30000000")}}) {
            assertThat(search.search(null, null, null, null, range[0], range[1], 0, 10))
                .extracting(j -> j.getId()).containsExactly(disclosed.getId());
            assertThat(search.countSearch(null, null, null, null, range[0], range[1])).isEqualTo(1);
        }
        assertThat(search.countSearch(null, null, null, null, null, null)).isEqualTo(3);
        assertThat(search.search(null, null, null, null, new BigDecimal("100000000"), new BigDecimal("120000000"), 0, 10)).isEmpty();
    }

    @Test void equalSortValuesHaveStableUniquePagesIncludingTheLastPage() {
        var ids = new java.util.ArrayList<Long>();
        for (int i = 0; i < 22; i++) {
            ids.add(job("Same title", false, new BigDecimal("15000000"), new BigDecimal("30000000")).getId());
        }
        for (String sort : new String[] {"createdAt", "title", "minSalary", "maxSalary"}) {
            for (String direction : new String[] {"asc", "desc"}) {
                var actual = new java.util.ArrayList<Long>();
                for (int page = 0; page < 3; page++) {
                    var result = search.search(null, null, null, null, null, null, page, 10, sort, direction);
                    assertThat(result).hasSize(page == 2 ? 2 : 10);
                    actual.addAll(result.stream().map(j -> j.getId()).toList());
                }
                var expected = new java.util.ArrayList<>(ids);
                if (direction.equals("desc")) java.util.Collections.reverse(expected);
                assertThat(actual).containsExactlyElementsOf(expected);
                assertThat(actual).doesNotHaveDuplicates();
            }
        }
    }
}
