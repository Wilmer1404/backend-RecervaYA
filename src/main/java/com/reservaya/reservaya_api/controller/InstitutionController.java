package com.reservaya.reservaya_api.controller;

import com.reservaya.reservaya_api.dto.InstitutionDTO;
import com.reservaya.reservaya_api.model.Institution;
import com.reservaya.reservaya_api.model.User;
import com.reservaya.reservaya_api.repository.InstitutionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;


//cuando un administrador entra a la configuración, solo vea y modifique los datos de la institución 
@RestController
@RequestMapping("/api/v1/institution")
@RequiredArgsConstructor
public class InstitutionController {

    private final InstitutionRepository institutionRepository;

    @GetMapping("/my-institution")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<InstitutionDTO> getMyInstitution(@AuthenticationPrincipal User adminUser) {
        Institution inst = adminUser.getInstitution();
        InstitutionDTO dto = new InstitutionDTO();
        dto.setName(inst.getName());
        dto.setContactEmail(inst.getContactEmail());
        dto.setPhone(inst.getPhone());
        dto.setWebsite(inst.getWebsite());
        dto.setAddress(inst.getAddress());
        return ResponseEntity.ok(dto);
    }

    @PutMapping("/my-institution")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<?> updateInstitution(@RequestBody InstitutionDTO request, @AuthenticationPrincipal User adminUser) {
        Institution inst = adminUser.getInstitution();
        
        inst.setName(request.getName());
        inst.setContactEmail(request.getContactEmail());
        inst.setPhone(request.getPhone());
        inst.setWebsite(request.getWebsite());
        inst.setAddress(request.getAddress());
        
        institutionRepository.save(inst);
        
        return ResponseEntity.ok(Map.of("message", "Institución actualizada correctamente"));
    }
}