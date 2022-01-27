/**********************************************************************
* This file is part of iDempiere ERP Open Source                      *
* http://www.idempiere.org                                            *
*                                                                     *
* Copyright (C) Contributors                                          *
*                                                                     *
* This program is free software; you can redistribute it and/or       *
* modify it under the terms of the GNU General Public License         *
* as published by the Free Software Foundation; either version 2      *
* of the License, or (at your option) any later version.              *
*                                                                     *
* This program is distributed in the hope that it will be useful,     *
* but WITHOUT ANY WARRANTY; without even the implied warranty of      *
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the        *
* GNU General Public License for more details.                        *
*                                                                     *
* You should have received a copy of the GNU General Public License   *
* along with this program; if not, write to the Free Software         *
* Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,          *
* MA 02110-1301, USA.                                                 *
*                                                                     *
* Contributors:                                                       *
* - Anozi Mada                                                        *
**********************************************************************/
package com.anozimada.idempiere.themepack.utils.process;
	
import java.util.logging.Level;

import org.adempiere.webui.apps.AEnv;
import org.compiere.model.MSysConfig;
import org.compiere.model.Query;
import org.compiere.process.ProcessInfoParameter;
import org.compiere.process.SvrProcess;
import org.compiere.util.AdempiereUserError;
import org.compiere.util.Msg;
import org.zkoss.lang.Library;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zul.theme.Themes;

/**
 *	Set the theme for ZK UI
 *
 * 	@author 	Anozi Mada
 */
@org.adempiere.base.annotation.Process
public class SetTheme extends SvrProcess {
	
	private String pBaseTheme =  "themepack";
	private String pTheme;
	private boolean pIsDefault = false;
	
	@Override
	protected void prepare()
	{
		ProcessInfoParameter[] para = getParameter();
		for (int i = 0; i < para.length; i++)
		{
			String name = para[i].getParameterName();
			if (para[i].getParameter() == null)
				;
			else if (name.equals("Value"))
				pBaseTheme = para[i].getParameter().toString();
			else if (name.equals("Theme"))
				pTheme = para[i].getParameter().toString();
			else if (name.equals("IsDefault"))
				pIsDefault = "Y".equals(para[i].getParameter());
			else
				log.log(Level.SEVERE, "Unknown Parameter: " + name);
		}
	}

	@Override
	protected String doIt() throws Exception {
		if (log.isLoggable(Level.INFO)) log.info("Base Theme = " + pBaseTheme + 
				", Theme=" + pTheme + 
				", IsDefault=" + pIsDefault);
		
		if (pTheme == null) {
			throw new AdempiereUserError(Msg.parseTranslation(getCtx(), "@Mandatory@: @Theme@"));
		}
		
		Runnable runnable;
		if (pIsDefault) {
			setBaseTheme();
			// set default theme for all end users
			runnable = new Runnable() {

				@Override
				public void run() {
					Library.setProperty("org.zkoss.theme.preferred", pTheme);
					Themes.setTheme(Executions.getCurrent(), pTheme);
					Executions.sendRedirect("");
				}
			};
			
			
		} else {
			// set theme for individual user
			runnable = new Runnable() {

				@Override
				public void run() {
					Themes.setTheme(Executions.getCurrent(), pTheme);
					Executions.sendRedirect("");
				}
			};
		}
		AEnv.executeAsyncDesktopTask(runnable);
		
		return "@Theme@ @Updated@";
	}
	
	private void setBaseTheme() {
		MSysConfig sysConfig = new Query(getCtx(), MSysConfig.Table_Name, "Name = 'ZK_THEME'", get_TrxName())
				.setClient_ID()
				.setOnlyActiveRecords(true)
				.first();
		if (sysConfig != null && !sysConfig.getValue().equals(pBaseTheme)) {
			sysConfig.setValue(pBaseTheme);
			sysConfig.saveEx();
		}
	}
}
