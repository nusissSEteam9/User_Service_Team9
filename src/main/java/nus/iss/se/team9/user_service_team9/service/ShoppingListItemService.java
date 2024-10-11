package nus.iss.se.team9.user_service_team9.service;

import jakarta.transaction.Transactional;
import nus.iss.se.team9.user_service_team9.model.ShoppingListItem;
import nus.iss.se.team9.user_service_team9.repo.ShoppingListItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Transactional
public class ShoppingListItemService {
	@Autowired
	ShoppingListItemRepository shoppingListItemRepo;

	public ShoppingListItem saveShoppingListItem(ShoppingListItem item) {
		return shoppingListItemRepo.save(item);
	}

	// get specific shoppingListItem by id
	public ShoppingListItem getShoppingListItemById(Integer id) {
		Optional<ShoppingListItem> shoppingListItem = shoppingListItemRepo.findById(id);
		return shoppingListItem.orElse(null);
	};
	
	// delete specific recipe by shoppingListItem
	public void deleteShoppingListItem(ShoppingListItem shoppingListItem) {
		shoppingListItemRepo.delete(shoppingListItem);
	}
}
