package vn.talentbridge;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import vn.talentbridge.adapter.out.persistence.adapter.CompanyRepositoryAdapter;
import vn.talentbridge.adapter.out.persistence.repository.CompanyJpaRepository;
import vn.talentbridge.core.domain.vo.CompanyStatus;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class CompanyRepositoryAdapterOrderingTest {

    private CompanyJpaRepository companyJpaRepository;
    private CompanyRepositoryAdapter adapter;

    @BeforeEach
    void setUp() {
        companyJpaRepository = mock(CompanyJpaRepository.class);
        adapter = new CompanyRepositoryAdapter(companyJpaRepository);
    }

    @Test
    void findAllOrdersNewestCompanyFirstWithStableIdTieBreak() {
        when(companyJpaRepository.searchCompanies(any(), any(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of()));

        adapter.findAll(0, 10, null, CompanyStatus.PENDING);

        var pageableCaptor = org.mockito.ArgumentCaptor.forClass(Pageable.class);
        verify(companyJpaRepository).searchCompanies(isNull(), eq(CompanyStatus.PENDING), pageableCaptor.capture());
        Sort sort = pageableCaptor.getValue().getSort();
        assertThat(sort.getOrderFor("createdAt")).isNotNull();
        assertThat(sort.getOrderFor("createdAt").getDirection()).isEqualTo(Sort.Direction.DESC);
        assertThat(sort.getOrderFor("id")).isNotNull();
        assertThat(sort.getOrderFor("id").getDirection()).isEqualTo(Sort.Direction.DESC);
    }
}
