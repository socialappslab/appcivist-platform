package models.transfer;

import models.Theme;

import java.util.ArrayList;
import java.util.List;

/**
 * Transfer for a list of transfer.
 *
 * @author Jorge Ramírez <jorge@codium.com.py>
 **/
public class ThemeListTransfer {

    private List<Theme> themes = new ArrayList<Theme>();


    public List<Theme> getThemes() {
        return themes;
    }

    public void setThemes(List<Theme> themes) {
        this.themes = themes;
    }
}
