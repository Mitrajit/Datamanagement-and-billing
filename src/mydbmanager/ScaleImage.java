/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mydbmanager;

import javax.swing.ImageIcon;

/**
 *
 * @author Mitrajit
 */
public class ScaleImage {
    public static ImageIcon scale(String loc,int width, int height){
     
        return new ImageIcon(new ImageIcon(LoginPage.class.getResource(loc)).getImage().getScaledInstance(width, height, java.awt.Image.SCALE_SMOOTH));
       
    }
    
}
