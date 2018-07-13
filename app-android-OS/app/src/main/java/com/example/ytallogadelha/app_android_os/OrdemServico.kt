package com.example.ytallogadelha.app_android_os

import android.os.Parcel
import android.os.Parcelable

data class OrdemServico(var idOS: Int?, var funcionarioOS: String, var descricaoOS: String, var feedbackOS: String): Parcelable {

    constructor(parcel: Parcel) : this(
            parcel.readValue(Int::class.java.classLoader) as? Int,
            parcel.readString(),
            parcel.readString(),
            parcel.readString())


    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeValue(idOS)
        parcel.writeString(funcionarioOS)
        parcel.writeString(descricaoOS)
        parcel.writeString(feedbackOS)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<OrdemServico> {
        override fun createFromParcel(parcel: Parcel): OrdemServico {
            return OrdemServico(parcel)
        }

        override fun newArray(size: Int): Array<OrdemServico?> {
            return arrayOfNulls(size)
        }
    }

    override fun toString(): String {
        return " ID: ${idOS} \n Funcioário: ${funcionarioOS} \n Descrição: ${descricaoOS}"
    }

}