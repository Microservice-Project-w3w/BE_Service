package com.equipmentrental.rental.service;

import com.equipmentrental.rental.dto.request.AppendixCreateRequest;
import com.equipmentrental.rental.dto.request.CancelContractRequest;
import com.equipmentrental.rental.dto.request.ContractCreateRequest;
import com.equipmentrental.rental.dto.request.ContractExtensionRequest;
import com.equipmentrental.rental.dto.response.ContractAppendixResponse;
import com.equipmentrental.rental.dto.response.RentalContractResponse;
import com.equipmentrental.rental.entity.*;
import com.equipmentrental.rental.exception.ApiException;
import com.equipmentrental.rental.mapper.RentalResponseMapper;
import com.equipmentrental.rental.repository.ContractAppendixRepository;
import com.equipmentrental.rental.repository.RentalContractRepository;
import com.equipmentrental.rental.repository.RentalOrderRepository;
import com.equipmentrental.rental.security.RentalDataScopeGuard;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ContractService {
    private final RentalContractRepository contracts;
    private final ContractAppendixRepository appendices;
    private final RentalOrderRepository orders;
    private final RentalDataScopeGuard dataScopeGuard;

    public ContractService(RentalContractRepository contracts, ContractAppendixRepository appendices,
                           RentalOrderRepository orders, RentalDataScopeGuard dataScopeGuard) {
        this.contracts = contracts; this.appendices = appendices; this.orders = orders; this.dataScopeGuard = dataScopeGuard;
    }

    public RentalContractResponse create(ContractCreateRequest request) {
        if (contracts.existsByRentalOrderId(request.rentalOrderId())) throw new ApiException("Đơn thuê đã có hợp đồng");
        RentalOrder order = orders.findById(request.rentalOrderId()).orElseThrow(() -> ApiException.notFound("Không tìm thấy đơn thuê"));
        dataScopeGuard.requireBranch(order.getOrganizationId(), order.getBranchId());
        if (order.getStatus() != OrderStatus.CONFIRMED) throw ApiException.invalidStatus("Chỉ tạo hợp đồng cho đơn đã xác nhận");
        RentalContract contract = new RentalContract();
        contract.setContractCode(code("CON")); contract.setOrganizationId(order.getOrganizationId()); contract.setBranchId(order.getBranchId());
        contract.setRentalOrderId(order.getId()); contract.setCustomerId(order.getCustomerId()); contract.setStartAt(order.getStartAt()); contract.setEndAt(order.getEndAt());
        contract.setTotalAmount(order.getTotalAmount()); contract.setTerms(request.terms()); contract.setStatus(ContractStatus.PENDING_APPROVAL);
        return RentalResponseMapper.contract(contracts.save(contract));
    }

    @Transactional(readOnly = true)
    public List<RentalContractResponse> list(Long organizationId, Long branchId) {
        dataScopeGuard.requireBranch(organizationId, branchId);
        return contracts.findByOrganizationIdAndBranchId(organizationId, branchId).stream().map(RentalResponseMapper::contract).toList();
    }

    @Transactional(readOnly = true)
    public RentalContractResponse get(Long id) { return RentalResponseMapper.contract(contract(id)); }
    public RentalContractResponse approve(Long id) {
        RentalContract contract = contract(id);
        if (contract.getStatus() != ContractStatus.PENDING_APPROVAL) throw ApiException.invalidStatus("Hợp đồng chưa ở trạng thái chờ phê duyệt");
        contract.approve(); return RentalResponseMapper.contract(contracts.save(contract));
    }
    public RentalContractResponse sign(Long id) {
        RentalContract contract = contract(id);
        if (contract.getStatus() != ContractStatus.APPROVED) throw ApiException.invalidStatus("Hợp đồng chưa được phê duyệt");
        contract.sign(); return RentalResponseMapper.contract(contracts.save(contract));
    }
    public RentalContractResponse cancel(Long id, CancelContractRequest request) {
        RentalContract contract = contract(id);
        if (contract.getStatus() == ContractStatus.LIQUIDATED || contract.getStatus() == ContractStatus.CANCELLED)
            throw ApiException.invalidStatus("Không thể hủy hợp đồng ở trạng thái hiện tại");
        contract.cancel(request.reason()); return RentalResponseMapper.contract(contracts.save(contract));
    }
    public RentalContractResponse liquidate(Long id) {
        RentalContract contract = contract(id);
        if (contract.getStatus() != ContractStatus.SIGNED && contract.getStatus() != ContractStatus.ACTIVE && contract.getStatus() != ContractStatus.EXTENDED)
            throw ApiException.invalidStatus("Chỉ thanh lý hợp đồng đã ký");
        contract.liquidate(); return RentalResponseMapper.contract(contracts.save(contract));
    }

    public ContractAppendixResponse createAppendix(Long contractId, AppendixCreateRequest request) {
        RentalContract contract = contract(contractId);
        if (contract.getStatus() != ContractStatus.SIGNED && contract.getStatus() != ContractStatus.ACTIVE && contract.getStatus() != ContractStatus.EXTENDED)
            throw ApiException.invalidStatus("Chỉ tạo phụ lục cho hợp đồng đang hiệu lực");
        if (request.appendixType() == AppendixType.EXTENSION && (request.newEndAt() == null || !request.newEndAt().isAfter(contract.getEndAt())))
            throw new ApiException("Ngày kết thúc gia hạn phải sau ngày kết thúc hiện tại");
        ContractAppendix appendix = new ContractAppendix();
        appendix.setAppendixCode(code("APP")); appendix.setOrganizationId(contract.getOrganizationId()); appendix.setBranchId(contract.getBranchId());
        appendix.setContractId(contract.getId()); appendix.setAppendixType(request.appendixType()); appendix.setStatus(AppendixStatus.PENDING_APPROVAL);
        appendix.setNewEndAt(request.newEndAt()); appendix.setTerms(request.terms());
        return RentalResponseMapper.appendix(appendices.save(appendix));
    }

    public ContractAppendixResponse createExtension(Long contractId, ContractExtensionRequest request) {
        return createAppendix(contractId, new AppendixCreateRequest(AppendixType.EXTENSION, request.newEndAt(), request.terms()));
    }

    @Transactional(readOnly = true)
    public List<ContractAppendixResponse> listAppendices(Long contractId) {
        contract(contractId);
        return appendices.findByContractIdOrderByCreatedAtDesc(contractId).stream().map(RentalResponseMapper::appendix).toList();
    }
    public ContractAppendixResponse approveAppendix(Long id) {
        ContractAppendix appendix = appendix(id);
        if (appendix.getStatus() != AppendixStatus.PENDING_APPROVAL) throw ApiException.invalidStatus("Phụ lục chưa ở trạng thái chờ phê duyệt");
        appendix.approve(); return RentalResponseMapper.appendix(appendices.save(appendix));
    }
    public ContractAppendixResponse signAppendix(Long id) {
        ContractAppendix appendix = appendix(id);
        if (appendix.getStatus() != AppendixStatus.APPROVED) throw ApiException.invalidStatus("Phụ lục chưa được phê duyệt");
        RentalContract contract = contract(appendix.getContractId());
        appendix.sign();
        if (appendix.getAppendixType() == AppendixType.EXTENSION) {
            contract.setEndAt(appendix.getNewEndAt()); contract.setStatus(ContractStatus.EXTENDED); contracts.save(contract);
        }
        return RentalResponseMapper.appendix(appendices.save(appendix));
    }

    private RentalContract contract(Long id) {
        RentalContract value = contracts.findById(id).orElseThrow(() -> ApiException.notFound("Không tìm thấy hợp đồng"));
        dataScopeGuard.requireBranch(value.getOrganizationId(), value.getBranchId()); return value;
    }
    private ContractAppendix appendix(Long id) {
        ContractAppendix value = appendices.findById(id).orElseThrow(() -> ApiException.notFound("Không tìm thấy phụ lục"));
        dataScopeGuard.requireBranch(value.getOrganizationId(), value.getBranchId()); return value;
    }
    private String code(String prefix) { return prefix + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase(); }
}
