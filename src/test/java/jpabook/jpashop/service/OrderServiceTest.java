package jpabook.jpashop.service;

import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.Member;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderStatus;
import jpabook.jpashop.domain.item.Book;
import jpabook.jpashop.domain.item.Item;
import jpabook.jpashop.exception.NotEnoughStockException;
import jpabook.jpashop.repository.OrderRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class OrderServiceTest {

    @Autowired
    EntityManager em;

    @Autowired
    OrderService orderService;

    @Autowired
    OrderRepository orderRepository;

    @Test
    void 상품주문() {
        // given
        Member member = createMember("user");
        Item book = createBook("Book", 10000, 10);

        // when
        int count = 2;
        Long orderId = orderService.order(member.getId(), book.getId(), count);

        // then
        Order order = orderRepository.findOne(orderId);

        assertThat(order.getStatus()).isEqualTo(OrderStatus.ORDER); // 주문 상태
        assertThat(order.getOrderItems().size()).isEqualTo(1); // 주문 상품 개수
        assertThat(order.getTotalPrice()).isEqualTo(10000 * count); // 주문 가격
        assertThat(book.getStockQuantity()).isEqualTo(10-count); // 남은 수량
    }

    @Test
    void 상품주문_재고수량초과() {
        // given
        Member member = createMember("user");
        Item book = createBook("Book", 10000, 1);

        // when
        int count = 2;

        // then
        assertThrows(NotEnoughStockException.class,
                () -> orderService.order(member.getId(), book.getId(), count));
    }

    @Test
    void 주문취소() {
        // given
        Member member = createMember("user");
        Item book = createBook("Book", 10000, 10);

        // when
        int count = 2;
        Long orderId = orderService.order(member.getId(), book.getId(), count);
        orderService.cancelOrder(orderId);

        // then
        Order order = orderRepository.findOne(orderId);

        assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCEL); // 주문 상태
        assertThat(book.getStockQuantity()).isEqualTo(10); // 남은 수량
    }

    private Member createMember(String name) {
        Member member = new Member();
        member.setName(name);
        member.setAddress(new Address("서울", "중랑구", "12345"));
        em.persist(member);
        return member;
    }

    private Item createBook(String name, int price, int quantity) {
        Item book = new Book();
        book.setName(name);
        book.setPrice(price);
        book.setStockQuantity(quantity);
        em.persist(book);
        return book;
    }
}