package nvm.poly.asm.service.impl;

import nvm.poly.asm.entity.Customer;
import nvm.poly.asm.entity.Order;
import nvm.poly.asm.entity.OrderDetail;
import nvm.poly.asm.entity.User;
import nvm.poly.asm.model.request.CustomerRequest;
import nvm.poly.asm.model.request.OrderRequest;
import nvm.poly.asm.repository.CustomerRepository;
import nvm.poly.asm.repository.OrderDetailRepository;
import nvm.poly.asm.repository.OrderRepository;
import nvm.poly.asm.repository.UserRepository;
import nvm.poly.asm.service.OrderService;
import nvm.poly.asm.service.ProductDetailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.List;
import java.util.UUID;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OrderDetailRepository orderDetailRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private ProductDetailService productDetailService;

    @Override
    public UUID add(OrderRequest orderRequest) {
        User user = userRepository.findById(UUID.fromString(orderRequest.getUserID())).get();
        Order order = orderRepository.save(Order.builder()
                .id(null)
                .user(user)
                .build());
        return order.getId();
    }

    @Override
    public Order findById(String id) {
        return orderRepository.findById(UUID.fromString(id)).get();
    }

    @Override
    @Transactional
    public String delete(String id) {
        Order order = orderRepository.findById(UUID.fromString(id)).get();
        List<OrderDetail> orderDetailList = orderDetailRepository.findAllByOrOrder(order);
        for (OrderDetail item : orderDetailList) {
            //Khi xoá order thì cập nhật lại số lượng của product-detail lên
            productDetailService.updateQuantityProductDetail(item.getProductDetail().getId(), item.getQuantity(), "plus");
        }
        order.setStatus(false);
        orderRepository.save(order);
        return "Xoá thành công";
    }



    @Override
    public String update(OrderRequest request, UUID id) {
        Order order = orderRepository.findById(id).get();
        User user = userRepository.findById(UUID.fromString(request.getUserID())).get();
        if (request.getCustomerID() != null) {
            Customer customer = customerRepository.findById(request.getCustomerID()).get();
            order.setCustomer(customer);
        }
        order.setPayment(request.getPayment());
        order.setQuantity(request.getQuantity());
        order.setTotalMoney(request.getTotalMoney());
        order.setStatus(request.getStatus());
        order.setUser(user);
        orderRepository.save(order);

        return "Sửa thành công";
    }

    @Override
    public String updateCustomer(CustomerRequest request, UUID id) {
        if (request.getAddress().isEmpty() || request.getAddress().isEmpty() || request.getPhoneNumber().isEmpty()) {
            return "";
        }
        Customer customer = customerRepository.save(Customer.builder()
                .id(request.getId())
                .address(request.getAddress())
                .fullName(request.getFullName())
                .phoneNumber(request.getPhoneNumber())
                .build());
        Order order = orderRepository.findById(id).get();
        order.setCustomer(customer);
        orderRepository.save(order);
        return null;
    }

    @Override
    public Page<Order> getAll(int page) {
        Pageable pageable = PageRequest.of(page - 1, 7);
        return orderRepository.getAllByPage(pageable);
    }
}
