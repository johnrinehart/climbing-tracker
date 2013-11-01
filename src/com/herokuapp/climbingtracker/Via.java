package com.herokuapp.climbingtracker;

public class Via {

	//private variables
    int _id;
    String _grau;
    String _cor;
    String _parede;
    int _quedas;
    boolean _completou;
     
    // Empty constructor
    public Via(){
         
    }
    // constructor
    public Via(int id, String grau, String cor, String parede, int quedas, boolean completou){
        this._id = id;
        this._grau = grau;
        this._cor = cor;
        this._parede = parede;
        this._quedas = quedas;
        this._completou = completou;
    }
     
    // constructor
    public Via(String grau, String cor, String parede, int quedas, boolean completou){
        this._grau = grau;
        this._cor = cor;
        this._parede = parede;
        this._quedas = quedas;
        this._completou = completou;
    }
    
    public int getID(){
        return this._id;
    }
     
    public String getGrau(){
        return this._grau;
    }
     
    public String getCor(){
        return this._cor;
    }
    
    public String getParede(){
        return this._parede;
    }
    
    public int getQuedas(){
        return this._quedas;
    }
    
    public boolean getCompletou(){
        return this._completou;
    }
    
	public void setID(int id) {
		this._id = id;
	}
	
	public void setGrau(String grau) {
		this._grau = grau;
	}
	
	public void setQuedas(int quedas) {
		this._quedas = quedas;
	}
	
	public void setParede(String cor) {
		this._cor = cor;
	}
	
	public void setCompletou(int completou) {
		this._completou = (completou == 1);		
	}
	
	public void setCor(String cor) {
		this._cor = cor;
	}
}
