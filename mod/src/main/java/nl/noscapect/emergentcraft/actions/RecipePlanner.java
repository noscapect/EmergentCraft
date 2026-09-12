package nl.noscapect.emergentcraft.actions;

import nl.noscapect.emergentcraft.AgentInventoryStore;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import java.util.*;

/** Uses vanilla recipe matching; it never owns a handwritten progression table. */
public final class RecipePlanner {
    public record Plan(String recipeId,String outputId,int outputCount,boolean needsTable,List<AgentInventoryStore.Entry> ingredients) { }
    private RecipePlanner() { }
    public static List<Plan> craftable(ServerLevel level,AgentInventoryStore inventory,UUID agent,int limit) {
        List<Plan> found=new ArrayList<>(); for(RecipeHolder<?> holder:level.getServer().getRecipeManager().getRecipes()) { if(!(holder.value() instanceof CraftingRecipe recipe)) continue; Plan plan=plan(level,inventory,agent,holder,recipe); if(plan!=null) found.add(plan); }
        found.sort(Comparator.comparing(Plan::outputId).thenComparing(Plan::recipeId)); List<Plan> unique=new ArrayList<>(); Set<String> outputs=new HashSet<>(); for(Plan plan:found) if(outputs.add(plan.outputId())) { unique.add(plan); if(unique.size()>=limit) break; } return unique;
    }
    public static Plan byRecipeId(ServerLevel level,AgentInventoryStore inventory,UUID agent,String recipeId) { for(RecipeHolder<?> holder:level.getServer().getRecipeManager().getRecipes()) if(holder.id().identifier().toString().equals(recipeId)&&holder.value() instanceof CraftingRecipe recipe) return plan(level,inventory,agent,holder,recipe); return null; }
    private static Plan plan(ServerLevel level,AgentInventoryStore inventory,UUID agent,RecipeHolder<?> holder,CraftingRecipe recipe) {
        List<Ingredient> ingredients=recipe.placementInfo().ingredients(); if(ingredients.isEmpty()||recipe.placementInfo().isImpossibleToPlace()) return null;
        Map<Item,Integer> available=new HashMap<>(); for(AgentInventoryStore.Entry entry:inventory.entries(agent)) { Item item=BuiltInRegistries.ITEM.getValue(Identifier.tryParse(entry.item())); if(item!=null) available.put(item,entry.count()); }
        List<ItemStack> inputs=new ArrayList<>(); List<AgentInventoryStore.Entry> used=new ArrayList<>(); for(Ingredient ingredient:ingredients) { Item selected=available.entrySet().stream().filter(entry->entry.getValue()>0&&ingredient.test(new ItemStack(entry.getKey()))).map(Map.Entry::getKey).min(Comparator.comparing(item->BuiltInRegistries.ITEM.getKey(item).toString())).orElse(null); if(selected==null) return null; available.merge(selected,-1,Integer::sum); String id=BuiltInRegistries.ITEM.getKey(selected).toString(); int index=used.stream().mapToInt(entry->entry.item().equals(id)?entry.count():0).sum(); if(index==0) used.add(new AgentInventoryStore.Entry(id,1)); else for(int n=0;n<used.size();n++) if(used.get(n).item().equals(id)) { used.set(n,new AgentInventoryStore.Entry(id,used.get(n).count()+1)); break; } inputs.add(new ItemStack(selected)); }
        int width=3,height=3; boolean needsTable=ingredients.size()>4; if(recipe instanceof ShapedRecipe shaped) { width=shaped.getWidth(); height=shaped.getHeight(); needsTable=width>2||height>2; }
        List<ItemStack> grid=new ArrayList<>(Collections.nCopies(width*height,ItemStack.EMPTY)); for(int index=0;index<inputs.size()&&index<grid.size();index++) grid.set(index,inputs.get(index)); CraftingInput input=CraftingInput.of(width,height,grid); if(!recipe.matches(input,level)) { width=3;height=3;grid=new ArrayList<>(Collections.nCopies(9,ItemStack.EMPTY));for(int index=0;index<inputs.size();index++)grid.set(index,inputs.get(index));input=CraftingInput.of(width,height,grid);if(!recipe.matches(input,level)) return null; }
        ItemStack output=recipe.assemble(input); if(output.isEmpty()) return null; return new Plan(holder.id().identifier().toString(),BuiltInRegistries.ITEM.getKey(output.getItem()).toString(),output.getCount(),needsTable,List.copyOf(used));
    }
}
