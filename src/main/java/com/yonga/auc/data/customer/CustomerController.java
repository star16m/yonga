package com.yonga.auc.data.customer;

import com.yonga.auc.common.YongaUtil;
import com.yonga.auc.config.ConfigService;
import com.yonga.auc.data.log.LogService;
import com.yonga.auc.mail.MailContents;
import com.yonga.auc.mail.MailService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.util.*;

@Slf4j
@Controller
public class CustomerController {
    @Autowired
    private CustomerRepository customerRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private LogService logService;
    @Autowired
    private ConfigService configService;
    @Autowired
    private MailService mailService;
    @GetMapping("/customer")
    public String customer(Map<String, Object> model) {
        List<Customer> customerList = this.customerRepository.findCustonersByDisplayTrue();
        model.put("customerList", customerList);
        return "customer/customer";
    }
    @PutMapping(value = "/customer/profile")
    public @ResponseBody ResponseEntity<String> updateCustomer(@Valid @RequestBody Customer customer, HttpServletRequest request) {
        try {
            validateCustomer(customer);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
        Customer oldCustomer = this.customerRepository.findByUserId(customer.getUserId());
        if (oldCustomer == null) {
            return new ResponseEntity<>("not found customer", HttpStatus.NOT_FOUND);
        }
        if (customer.getPassword() != null && !customer.getPassword().isEmpty()) {
            customer.setPassword(passwordEncoder.encode(customer.getPassword()));
        } else {
            customer.setPassword(oldCustomer.getPassword());
        }
        customer.setPrivilege(oldCustomer.getPrivilege());
        customer.setEnabled(oldCustomer.getEnabled());
        customer.setDisplay(oldCustomer.getDisplay());
        this.customerRepository.save(customer);
        String updateUser = request.getUserPrincipal().getName();
        this.logService.addLog("유저 [" + customer.getUserId() + "] 를 수정 하였습니다. by[" + updateUser + "]");
        return new ResponseEntity<>("success", HttpStatus.OK);
    }
    @PostMapping("/customer/join")
    public @ResponseBody ResponseEntity<Map<String, String>> createCustomer(@Valid @RequestBody Customer customer, HttpServletRequest request) {
        try {
            validateCustomer(customer);
        } catch (Exception e) {
            return new ResponseEntity<>(Collections.singletonMap("result", e.getMessage()), HttpStatus.BAD_REQUEST);
        }
        if (this.customerRepository.findByUserId(customer.getUserId()) != null) {
            return new ResponseEntity<>(Collections.singletonMap("result", "이미 존재하는 id 입니다. [" + customer.getUserId() + "]"), HttpStatus.BAD_REQUEST);
        }
        customer.setPassword(passwordEncoder.encode(customer.getPassword()));
        customer.setPrivilege("ROLE_USER");
        customer.setEnabled(request.isUserInRole("ADMIN"));
        this.customerRepository.save(customer);
        String adminEmail = this.configService.getConfigValue("CONFIG", "ADMIN_EMAIL");
        String mailId = this.configService.getConfigValue("CONFIG", "MAIL_ID");
        if (YongaUtil.isNotEmpty(mailId)) {
            try {
                this.mailService.sendEmail(new MailContents("고객 가입 승인 요청", "가입 승인 요청이 있습니다.",
                        Arrays.asList("승인 요청 시간 : [" + new Date() + "]"),
                        Arrays.asList(""),
                        Arrays.asList("고객 ID : [" + customer.getUserId() + "]",
                                "고객 이름 : [" + customer.getName() + "]",
                                "고객 email : [" + customer.getEmail() + "]")
                ), adminEmail);
            } catch (Exception e) {
                this.logService.addLog("메일 발송 중 에러가 발생하였습니다. error [" + e.getMessage() + "]");
            }
        }
        return new ResponseEntity<>(Collections.singletonMap("result", "success"), HttpStatus.OK);
    }
    @PatchMapping("/customer/enabled")
    public @ResponseBody ResponseEntity<Map<String, String>> updateCustomer(@RequestBody @Valid CustomerEnabled customerEnabled) {

        Customer customer = this.customerRepository.findByUserId(customerEnabled.getCustomerId());
        if (customer == null) {
            return new ResponseEntity<>(Collections.singletonMap("result", "유저 정보를 찾을 수 없습니다."), HttpStatus.BAD_REQUEST);
        }
        if (customer.getUserId().equalsIgnoreCase("admin")) {
            return new ResponseEntity<>(Collections.singletonMap("result", "관리자는 수정할 수 없습니다."), HttpStatus.BAD_REQUEST);
        }
        customer.setEnabled(customerEnabled.getEnabled());
        this.customerRepository.save(customer);
        return new ResponseEntity<>(Collections.singletonMap("result", customerEnabled.getCustomerId()+ " is " + customerEnabled.getEnabled()), HttpStatus.OK);
    }
    @DeleteMapping(value = "/customer/{customerId}")
    public @ResponseBody ResponseEntity<Map<String, String>> deleteCustomer(@PathVariable String customerId) {

        Customer customer = this.customerRepository.findByUserId(customerId);
        if (customer == null) {
            return new ResponseEntity<>(Collections.singletonMap("result", "유저 정보를 찾을 수 없습니다."), HttpStatus.BAD_REQUEST);
        }
        if (customer.getUserId().equalsIgnoreCase("admin")) {
            return new ResponseEntity<>(Collections.singletonMap("result", "관리자는 삭제할 수 없습니다."), HttpStatus.BAD_REQUEST);
        }
        this.customerRepository.deleteById(customerId);
        return new ResponseEntity<>(Collections.singletonMap("result", customerId+ " is deleted!!!"), HttpStatus.OK);
    }
    @GetMapping("/signup")
    public String signup(Map<String, Object> model) {
        Customer customer = new Customer();
        model.put("customer", customer);
        model.put("MODE", "SIGNUP");
        return "customer/modifyCustomer";
    }
    @GetMapping("/customer/download")
    public void downloadCsv(HttpServletResponse httpServletResponse) throws IOException {
        httpServletResponse.setContentType("text/csv");
        httpServletResponse.setHeader("Content-Disposition", "attachment; file=users.csv");
        httpServletResponse.setCharacterEncoding("UTF-8");
        PrintWriter writer = httpServletResponse.getWriter();
        CSVPrinter csvPrinter = new CSVPrinter(writer,
                CSVFormat.DEFAULT.withHeader("userId", "password", "privilege", "name", "tel", "email", "description", "enabled", "display"));
        for (Customer customer : this.customerRepository.findAllCustomer()) {
            csvPrinter.printRecord(Arrays.asList(
                    customer.getUserId(),
                    customer.getPassword(),
                    customer.getPrivilege(),
                    customer.getName(),
                    customer.getTel(),
                    customer.getEmail(),
                    customer.getDescription(),
                    customer.getEnabled(),
                    customer.getDisplay()
                    ));
        }
    }

    @PostMapping("/customer/upload")
    public String uploadFile(@RequestParam("file") MultipartFile file, RedirectAttributes attributes) throws IOException {

        // check if file is empty
        if (file.isEmpty()) {
            attributes.addFlashAttribute("message", "Please select a file to upload.");
            return "redirect:/";
        }
        final Reader reader = new InputStreamReader(file.getInputStream(), "UTF-8");
        final CSVParser parser = new CSVParser(reader, CSVFormat.DEFAULT.withHeader());
        try {
            for (final CSVRecord record : parser) {
                final String userId = record.get("userId");
                final String password = record.get("password");
                final String privilege = record.get("privilege");
                final String name = record.get("name");
                final String tel = record.get("tel");
                final String email = record.get("email");
                final String description = record.get("description");
                final String enabled = record.get("enabled");
                final String display = record.get("display");
                Customer customer = new Customer();
                customer.setUserId(userId);
                customer.setPassword(password);
                customer.setPrivilege(privilege);
                customer.setName(name);
                customer.setTel(tel);
                customer.setEmail(email);
                customer.setDescription(description);
                customer.setEnabled(Boolean.valueOf(enabled));
                customer.setDisplay(Boolean.valueOf(display));
                this.customerRepository.save(customer);
//            private String userId;
//            private String password;
//            private String privilege;
//            private String name;
//            private String tel;
//            private String email;
//            private String description;
//            private Boolean enabled = false;
//            private Boolean display = true;
            }
        } finally {
            parser.close();
            reader.close();
        }

        // return success response
        attributes.addFlashAttribute("message", "You successfully uploaded !");

        return "redirect:/customer";
    }

    @GetMapping("/customer/{customerId}")
    public String getCustomerById(@PathVariable String customerId, Map<String, Object> model, HttpServletRequest request) {
        log.debug("this is getCustomerById [{}]", customerId);
        Customer customer = null;
        if ("NEW".equalsIgnoreCase(customerId)) {
            customer = new Customer();
            model.put("customer", customer);
            model.put("MODE", "NEW");
        } else {
            customer = this.customerRepository.findByUserId(customerId);
            if (customer == null) {
                return "/error/404";
            }
            if (request.getUserPrincipal() != null && !request.isUserInRole("ADMIN") && !customerId.equals(request.getUserPrincipal().getName())) {
                return "/error/403";
            }
            model.put("customer", customer);
            model.put("MODE", "MODIFY");
        }
        return "customer/modifyCustomer";
    }

    private void validateCustomer(Customer customer) throws Exception {
        if ("NEW".equals(customer.getUserId())) {
            throw new Exception("user id [NEW] 는 예약어입니다.");
        }
    }
}
