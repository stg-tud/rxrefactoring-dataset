/*
 * Copyright (C) 2013 DobinRutishauser@broken.ch
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package gui.networking;

import attacks.AttackMain;
import gui.botLeft.PanelLeftOptions;
import gui.botLeft.PanelLeftUi;
import model.SentinelHttpMessageAtk;
import model.SentinelHttpMessageOrig;
import model.SentinelHttpParam;

/**
 *
 * @author dobin
 */
public class AttackWorkEntry {
    public SentinelHttpParam attackHttpParam;
    public AttackMain.AttackTypes attackType;
    public String options;
    public SentinelHttpMessageOrig origHttpMessage;
    public PanelLeftUi panelParent;
    public boolean followRedirect;
    public PanelLeftOptions.InsertPositions insertPosition;
    public String mainSessionName;
    
    SentinelHttpMessageAtk result;
    
    public AttackWorkEntry(SentinelHttpParam attackHttpParam, 
            AttackMain.AttackTypes attackType,
            String options,
            SentinelHttpMessageOrig origHttpMessage, 
            PanelLeftUi panelParent, 
            boolean followRedirect, 
            PanelLeftOptions.InsertPositions insertPosition,
            String mainSessionName) {
     
        this.attackHttpParam = attackHttpParam;
        this.attackType = attackType;
        this.origHttpMessage = origHttpMessage;
        this.options = options;
        this.panelParent = panelParent;
        this.followRedirect = followRedirect;
        this.insertPosition = insertPosition;
        this.mainSessionName = mainSessionName;
    }

}
