package enums;
/**
 * "PUBLIC" => everyone can view everything, and everyone can also request a membership
 * "LISTED" => only the basic information about the assembly is available (name, address, city, state, country, icon, cover) + categories
 * "PRIVATE" => Only the name of the assembly is visible
 * 
 * @author cdparra
 */
public enum Visibility {
    PUBLIC, HIDDEN
}
