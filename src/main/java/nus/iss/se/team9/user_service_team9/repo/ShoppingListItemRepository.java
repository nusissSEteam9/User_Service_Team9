package nus.iss.se.team9.user_service_team9.repo;

import nus.iss.se.team9.user_service_team9.model.ShoppingListItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ShoppingListItemRepository extends JpaRepository<ShoppingListItem,Integer>{
    List<ShoppingListItem> findByIdInAndMemberId(List<Integer> ids, Integer memberId);
}
